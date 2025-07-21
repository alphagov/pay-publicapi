FROM maven:3.9-eclipse-temurin-11-alpine@sha256:2f46725caffb5ddb97fb671262b12da9270075095fecc3203ac332fb4141ef69 AS builder

WORKDIR /home/build
COPY . .

RUN ["mvn", "clean", "--no-transfer-progress", "package", "-DskipTests"]

FROM eclipse-temurin:21-jre-alpine@sha256:7b5c88eb4182a92aab3a4b10550061a6e18639bf176e7ebca21f866b19f853c1 AS final

RUN ["apk", "--no-cache", "upgrade"]

ARG DNS_TTL=10

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN echo networkaddress.cache.ttl=$DNS_TTL >> "$JAVA_HOME/conf/security/java.security"

RUN ["apk", "add", "--no-cache", "bash", "tini"]

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

COPY --from=builder /home/build/docker-startup.sh .
COPY --from=builder /home/build/target/*.yaml .
COPY --from=builder /home/build/target/pay-*-allinone.jar .

ENTRYPOINT ["tini", "-e", "143", "--"]

CMD ["bash", "./docker-startup.sh"]
