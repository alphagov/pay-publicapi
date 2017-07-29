FROM openjdk:8-jre-alpine

RUN apk update
RUN apk upgrade

RUN apk add bash

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/
ADD docker-startup.sh /app/docker-startup.sh

CMD bash ./docker-startup.sh
