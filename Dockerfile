FROM eclipse-temurin:21.0.4_7-jdk-noble

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
