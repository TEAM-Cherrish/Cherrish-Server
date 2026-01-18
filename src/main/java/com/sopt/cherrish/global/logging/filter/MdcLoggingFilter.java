package com.sopt.cherrish.global.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String CLIENT_IP = "clientIp";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String requestId = generateRequestId();
            setMdcContext(request, requestId);
            response.setHeader(REQUEST_ID_HEADER, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void setMdcContext(HttpServletRequest request, String requestId) {
        MDC.put(REQUEST_ID, requestId);
        MDC.put(METHOD, request.getMethod());
        MDC.put(URI, request.getRequestURI());
        MDC.put(CLIENT_IP, getClientIp(request));

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            MDC.put(USER_ID, userId);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
