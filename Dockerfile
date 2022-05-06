FROM eclipse-temurin:17.0.3_7-jre-focal

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
