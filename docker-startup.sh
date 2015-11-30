#!/usr/bin/env bash
if [ "${ENABLE_NEWRELIC}" == "yes" ]; then
  NEWRELIC_JVM_FLAG="-javaagent:/app/newrelic/newrelic.jar"
fi
java ${NEWRELIC_JVM_FLAG} -jar *-allinone.jar server *.yaml
