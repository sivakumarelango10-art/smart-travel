# ==============================================================================
# SmartTravel Backend — Multi-stage Production Dockerfile (Java 21 LTS)
# Stage 1: Build the application JAR using Maven
# Stage 2: Slim runtime image with non-root user
# ==============================================================================

# ----- Stage 1: Build Stage (Maven + JDK 21) --------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper and pom first (layer-cache for dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable and download dependencies (cached layer if pom unchanged)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source code
COPY src/ src/

# Build the fat JAR, skip tests (tests run separately in CI)
RUN ./mvnw clean package -DskipTests -q

# ----- Stage 2: Runtime Stage (JRE 21, non-root) ----------------------------
FROM eclipse-temurin:21-jre-jammy AS runner

# Create non-root system user and group for runtime security
RUN groupadd -r smarttravel && useradd -r -g smarttravel -d /app -s /sbin/nologin smarttravel

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /build/target/smarttravel-backend-*.jar app.jar

# Ensure proper ownership
RUN chown -R smarttravel:smarttravel /app

USER smarttravel

# Default HTTP server port (overridden by Render's PORT env var)
EXPOSE 8080

# Environment defaults (overridden by Render dashboard environment variables)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Container healthcheck using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Dserver.port=${SERVER_PORT} -jar app.jar"]
