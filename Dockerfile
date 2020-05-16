FROM adoptopenjdk:11.0.7_10-jdk-hotspot

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew
RUN mkdir /root/.gradle
