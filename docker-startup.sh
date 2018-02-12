#!/usr/bin/env bash

java ${JAVA_OPTS} -jar *-allinone.jar buildTrustStore *.yaml
java ${JAVA_OPTS} -jar *-allinone.jar server *.yaml
