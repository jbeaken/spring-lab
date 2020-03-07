FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine-slim

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
ARG SERVICE_NAME

COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-Xmx128m -Xms64m"
ENV SERVICE_NAME=${SERVICE_NAME}

LABEL APP=${SERVICE_NAME}
LABEL DOMAIN="gateway"

ENTRYPOINT ["java","-jar","/app.jar"]