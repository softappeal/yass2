FROM eclipse-temurin:17.0.1_12-jdk-focal

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
