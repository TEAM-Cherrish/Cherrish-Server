# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build application
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
