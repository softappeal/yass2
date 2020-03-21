FROM adoptopenjdk:11.0.6_10-jdk-hotspot

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew
RUN mkdir /root/.gradle
