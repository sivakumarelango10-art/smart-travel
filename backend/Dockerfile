# ==============================================================================
# SmartTravel Backend — Multi-stage Production Dockerfile (Java 21 LTS)
# ==============================================================================

FROM eclipse-temurin:21-jre-jammy AS runner

# Create non-root system user and group for runtime security
RUN groupadd -r smarttravel && useradd -r -g smarttravel -d /app -s /sbin/nologin smarttravel

WORKDIR /app

# Copy production executable JAR
COPY target/smarttravel-backend-1.0.0.jar app.jar

# Ensure proper ownership
RUN chown -R smarttravel:smarttravel /app

USER smarttravel

# Default HTTP server port
EXPOSE 8080

# Environment defaults (overridden by host environment / secrets manager)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Container healthcheck using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
