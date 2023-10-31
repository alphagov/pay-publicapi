FROM eclipse-temurin:11-jre-alpine@sha256:c033de8d3135b954d230eed6b9bd14dc3dbf32550e448705222f1c907ceebe08

RUN ["apk", "--no-cache", "upgrade"]

ARG DNS_TTL=15

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN echo networkaddress.cache.ttl=$DNS_TTL >> "$JAVA_HOME/conf/security/java.security"

RUN ["apk", "add", "--no-cache", "bash", "tini"]

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD docker-startup.sh /app/docker-startup.sh
ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/

ENTRYPOINT ["tini", "-e", "143", "--"]

CMD ["bash", "./docker-startup.sh"]
