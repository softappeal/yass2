FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-ubuntu-slim

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
