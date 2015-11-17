FROM                java:8-jre
ENV JAVA_HOME       /usr/lib/jvm/java-8-*/
WORKDIR             /app
ADD                 target/*.yaml /app/
ADD                 target/pay-*-allinone.jar /app/
CMD                 java -jar *-allinone.jar server *.yaml
