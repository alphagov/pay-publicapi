FROM maven:3.9.5-eclipse-temurin-11@sha256:d698c543af44a1a8e4b2b2f9c1cfe3e009f2398cc7125a3d1204c71ad876800f AS builder

WORKDIR /home/build
COPY . .

RUN ["mvn", "clean", "--no-transfer-progress", "package", "-DskipTests"]

FROM eclipse-temurin:11-jre@sha256:e83a781caa11c13bd25f3722a6735ce39f19186cba6d632566408c0a9dd70aee AS final

RUN ["apt-get", "update"]
RUN ["apt-get", "upgrade", "-y"]

ARG DNS_TTL=15

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN echo networkaddress.cache.ttl=$DNS_TTL >> "$JAVA_HOME/conf/security/java.security"

RUN ["apt-get", "install", "-y", "bash", "tini"]

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
