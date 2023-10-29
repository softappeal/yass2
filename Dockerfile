FROM eclipse-temurin:17.0.9_9-jdk-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
