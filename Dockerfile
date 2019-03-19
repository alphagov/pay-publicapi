FROM govukpay/openjdk:alpine-3.9-jre-base-8.201.08

RUN apk --no-cache upgrade

RUN apk --no-cache add bash

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD docker-startup.sh /app/docker-startup.sh
ADD run-with-chamber.sh /app/run-with-chamber.sh
ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/

CMD bash ./docker-startup.sh
