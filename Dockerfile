# syntax=docker/dockerfile:1.7

############################
# Build stage
############################
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace

# Leverage Gradle dependency cache first
COPY build.gradle settings.gradle /workspace/
COPY gradle /workspace/gradle
RUN gradle --version > /dev/null

# Copy project sources
COPY . /workspace

# Build Spring Boot executable jar
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew clean bootJar --no-daemon && \
    JAR_PATH=$(ls build/libs/*.jar | grep -v '\-plain.jar' | head -n 1) && \
    mv "$JAR_PATH" build/libs/app.jar

############################
# Runtime stage
############################
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul \
    SPRING_PROFILES_ACTIVE=local-dev
WORKDIR /app

COPY --from=builder /workspace/build/libs/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
