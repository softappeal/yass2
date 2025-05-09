# https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:21.0.6_7-jdk-noble

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
