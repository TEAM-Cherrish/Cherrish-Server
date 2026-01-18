package com.sopt.cherrish.global.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DiscordEmbedAppender extends AppenderBase<ILoggingEvent> {

    private static final int COLOR_ERROR = 15548997;  // 빨간색 (#ED4245)
    private static final int MAX_FIELD_LENGTH = 1024;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.of("Asia/Seoul"));

    private String webhookUrl;
    private String serviceName = "Cherrish";
    private HttpClient httpClient;

    @Override
    public void start() {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            addWarn("Discord webhook URL is not set. Discord appender will be disabled.");
            return;
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            String json = buildEmbedJson(event);
            sendToDiscord(json);
        } catch (Exception e) {
            addError("Failed to send Discord notification", e);
        }
    }

    private String buildEmbedJson(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();

        String time = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String method = mdc.getOrDefault("method", "-");
        String uri = mdc.getOrDefault("uri", "-");
        String requestId = mdc.getOrDefault("requestId", "-");
        String userId = mdc.getOrDefault("userId", "-");
        String message = escapeJson(event.getFormattedMessage());
        String stackTrace = extractStackTrace(event);
        String location = extractLocation(event);

        return """
            {
              "username": "🍒 %s Error Bot",
              "embeds": [{
                "title": "🚨 SERVER ERROR",
                "color": %d,
                "fields": [
                  {"name": "🌐 API", "value": "`%s` **%s**", "inline": false},
                  {"name": "⏰ Time", "value": "`%s`", "inline": true},
                  {"name": "📍 Location", "value": "`%s`", "inline": true},
                  {"name": "🔑 Request ID", "value": "`%s`", "inline": true},
                  {"name": "👤 User ID", "value": "`%s`", "inline": true},
                  {"name": "💬 Message", "value": "%s", "inline": false},
                  {"name": "📋 Stack Trace", "value": "```java\\n%s```", "inline": false}
                ],
                "footer": {"text": "%s Server"}
              }]
            }
            """.formatted(
                serviceName,
                COLOR_ERROR,
                method, uri,
                time,
                location,
                requestId,
                userId,
                message,
                stackTrace,
                serviceName
            );
    }

    private String extractLocation(ILoggingEvent event) {
        StackTraceElement[] callerData = event.getCallerData();
        if (callerData != null && callerData.length > 0) {
            StackTraceElement caller = callerData[0];
            return caller.getClassName().substring(
                    Math.max(0, caller.getClassName().lastIndexOf('.') + 1)
                ) + "." + caller.getMethodName() + ":" + caller.getLineNumber();
        }
        return event.getLoggerName();
    }

    private String extractStackTrace(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {
            return "No stack trace";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwableProxy.getClassName())
          .append(": ")
          .append(throwableProxy.getMessage())
          .append("\\n");

        StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        int linesToShow = Math.min(5, stackTrace.length);
        for (int i = 0; i < linesToShow; i++) {
            sb.append("  at ").append(stackTrace[i].getSTEAsString()).append("\\n");
        }

        if (stackTrace.length > linesToShow) {
            sb.append("  ... ").append(stackTrace.length - linesToShow).append(" more");
        }

        String result = sb.toString();
        if (result.length() > MAX_FIELD_LENGTH - 20) {
            result = result.substring(0, MAX_FIELD_LENGTH - 20) + "\\n...truncated";
        }
        return escapeJson(result);
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendToDiscord(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 400) {
                            addError("Discord API error: " + response.statusCode() + " - " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        addError("Failed to send Discord notification", e);
                        return null;
                    });
        } catch (Exception e) {
            addError("Failed to send Discord notification", e);
        }
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
