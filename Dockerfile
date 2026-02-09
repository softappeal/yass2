# https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:25.0.2_10-jdk-noble

# NOTE: fixes /root/.gradle/nodejs/node-v25.0.0-linux-x64/bin/node: error while loading shared libraries: libatomic.so.1: cannot open shared object file: No such file or directory
RUN apt-get update && apt-get -y install libatomic1

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew

RUN mkdir /root/.gradle
