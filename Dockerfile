# Digest of image e.g. openjdk11:jre-11.0.7_10-alpine for linux/amd64 from tags list on https://hub.docker.com/r/adoptopenjdk/openjdk11
FROM adoptopenjdk/openjdk11@sha256:63a55a2b49a12a15409c8228e7a23be3347b565d45dc7d05a03f965b495e0a90

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
