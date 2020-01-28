FROM amazoncorretto:11.0.6

COPY  . /project
WORKDIR /project

RUN chmod +x ./gradlew
RUN mkdir /root/.gradle
