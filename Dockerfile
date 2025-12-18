FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
