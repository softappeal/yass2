FROM eclipse-temurin:17.0.7_7-jre-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
