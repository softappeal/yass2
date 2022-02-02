FROM eclipse-temurin:17.0.2_8-jre-focal

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
