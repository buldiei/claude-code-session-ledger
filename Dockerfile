# syntax=docker/dockerfile:1

# ---- 1. Build the SvelteKit frontend to a static SPA ----
FROM node:22-alpine AS frontend
WORKDIR /fe
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build        # outputs /fe/build (static SPA with index.html fallback)

# ---- 2. Build the Spring Boot jar, with the frontend baked into static/ ----
FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /app
COPY pom.xml ./
# warm the dependency cache (best-effort; safe to fail)
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
COPY --from=frontend /fe/build ./src/main/resources/static
RUN mvn -q -B -DskipTests clean package

# ---- 3. Runtime ----
FROM eclipse-temurin:25-jre
LABEL org.opencontainers.image.title="session-ledger" \
      org.opencontainers.image.description="Store Claude Code sessions as versioned cards (web R/D, MCP C/U)" \
      org.opencontainers.image.version="1.0.2"
WORKDIR /app
# SQLite (standalone profile) writes here; xerial won't create the parent dir itself.
# With docker-compose.standalone.yml this is also the volume mount point.
RUN mkdir -p /app/data
COPY --from=backend /app/target/session-ledger-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
