FROM java:8-jre
ENV JAVA_HOME /usr/lib/jvm/java-8-*/
ENV ENABLE_NEWRELIC no

WORKDIR /app
EXPOSE 8080 8081

ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/
ADD docker-startup.sh /app/docker-startup.sh
ADD newrelic/* /app/newrelic/

CMD bash ./docker-startup.sh