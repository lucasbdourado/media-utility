# Build Stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Install Node.js 20 & NPM for React build orchestration
RUN apt-get update && apt-get install -y curl gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-noble

# Install runtime packages (ffmpeg, python3, curl)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    python3 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install yt-dlp official binary
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp

# Configure non-root user and group
RUN groupadd -g 10001 spring && \
    useradd -u 10001 -g spring -m -d /app -s /bin/bash appuser

WORKDIR /app
RUN mkdir -p /app/storage && chown -R appuser:spring /app

# Copy packaged JAR from builder stage
COPY --from=builder /build/target/media-utility-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:spring app.jar

USER appuser

ENV PORT=8080
ENV MEDIA_UTILITY_STORAGE_ROOT=/app/storage

EXPOSE 8080
VOLUME ["/app/storage"]

ENTRYPOINT ["java", "-jar", "app.jar"]
