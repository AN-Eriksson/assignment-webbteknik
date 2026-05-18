# --------------------------
# 1. Frontend build (Vite)
# --------------------------
FROM node:22 AS frontend-build
WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


# --------------------------
# 2. Spring Boot build
# --------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle wrapper + config
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Backend source
COPY src src

# Kopiera frontend BEFORE build
COPY --from=frontend-build /frontend/dist src/main/resources/static

# Säkerställ rätt permissions
RUN chmod +x gradlew \
    && ./gradlew clean bootJar -x test


# --------------------------
# 3. Runtime image
# --------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]