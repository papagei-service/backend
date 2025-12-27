# References: https://ktor.io/docs/docker.html#manual

# Stage 1: Build application
FROM gradle:8.10-jdk21-alpine AS build
ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle buildFatJar --no-daemon

# Stage 2: Test image
FROM gradle:8.10-jdk21-alpine AS test
ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /home/gradle/src
COPY --from=build /home/gradle/.gradle /home/gradle/.gradle
COPY --chown=gradle:gradle . .
ENTRYPOINT ["gradle", "test", "--no-daemon"]

# Stage 3: Production image
FROM amazoncorretto:21-alpine3.21 AS prod
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./papagei.jar
COPY --from=build /home/gradle/src/migrations ./migrations
EXPOSE 8080
# Create non-root user
RUN addgroup -S app && adduser -S app -G app
USER app
ENTRYPOINT ["java","-jar","papagei.jar"]