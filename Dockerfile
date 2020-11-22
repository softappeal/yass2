FROM adoptopenjdk:11.0.9_11-jdk-hotspot

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew
RUN mkdir /root/.gradle
