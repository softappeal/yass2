FROM eclipse-temurin:11.0.13_8-jdk-focal

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
