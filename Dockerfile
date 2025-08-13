# https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:21.0.8_9-jdk-noble

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
