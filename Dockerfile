FROM eclipse-temurin:11-jre-alpine@sha256:14a756e2626f8ef78e6fee63c86dbc7da85c4850760c8875b831e1af6de29998

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
