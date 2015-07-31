FROM java:8-jre
ENV JAVA_HOME /usr/lib/jvm/java-8-*/

ADD target/pay-*-allinone.jar /app/
ADD target/*.yaml /app/
WORKDIR /app

EXPOSE 8080 8081

CMD java -jar pay-*-allinone.jar server *.yaml