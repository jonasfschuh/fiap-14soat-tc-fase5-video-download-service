FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

ARG NEW_RELIC_AGENT_VERSION=8.25.0
RUN apk add --no-cache curl \
    && mkdir -p /app/newrelic \
    && curl -sSL \
       "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-agent-${NEW_RELIC_AGENT_VERSION}.jar" \
       -o /app/newrelic/newrelic.jar

WORKDIR /app
COPY --from=build /app/application/target/video-download-application-1.0.0-exec.jar app.jar
COPY newrelic/newrelic.yml /app/newrelic/newrelic.yml
EXPOSE 8086
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-Dnewrelic.config.file=/app/newrelic/newrelic.yml", "-jar", "app.jar"]
