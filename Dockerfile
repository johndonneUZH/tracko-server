# Build stage
FROM gradle:7.6-jdk17 AS build

WORKDIR /app

# Copy gradle files first to leverage cache
COPY gradlew gradlew.bat gradle.properties /app/
COPY gradle /app/gradle
RUN chmod +x ./gradlew

# Copy build files
COPY build.gradle settings.gradle /app/

# Copy source code
COPY src /app/src

# Build with dependency caching
RUN ./gradlew clean build --no-daemon --stacktrace

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080

WORKDIR /app

# Copy built artifact
COPY --from=build /app/build/libs/*.jar /app/application.jar

# Use a non-root user (more compatible with App Engine)
RUN useradd -m myuser && \
    chown -R myuser:myuser /app
USER myuser

EXPOSE 8080

# Use exec form for proper signal handling
ENTRYPOINT ["java", "-jar", "/app/application.jar"]