FROM gradle:8.11.1-jdk21 AS build
WORKDIR /workspace
COPY build.gradle.kts settings.gradle.kts ./
COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 anchorguard
COPY --from=build /workspace/build/libs/*.jar app.jar
USER anchorguard
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
