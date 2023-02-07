FROM eclipse-temurin:17.0.6_10-jre-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
