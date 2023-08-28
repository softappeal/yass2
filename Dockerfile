FROM eclipse-temurin:17.0.8_7-jdk-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
