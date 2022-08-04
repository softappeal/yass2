FROM eclipse-temurin:17.0.4_8-jre-jammy

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
