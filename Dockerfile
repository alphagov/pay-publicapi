FROM govukpay/openjdk:adoptopenjdk-jre-11.0.3.7-alpine

RUN apk --no-cache upgrade

RUN apk --no-cache add bash

ENV JAVA_HOME /opt/java/openjdk
ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD docker-startup.sh /app/docker-startup.sh
ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/

CMD bash ./docker-startup.sh
