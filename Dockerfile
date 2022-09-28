FROM eclipse-temurin:17.0.4.1_1-jre-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
