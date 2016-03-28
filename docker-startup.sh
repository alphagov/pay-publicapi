#!/usr/bin/env bash

if [ "${ENABLE_NEWRELIC}" == "yes" ]; then
  NEWRELIC_JVM_FLAG="-javaagent:/app/newrelic/newrelic.jar"
fi

# (re)create cacerts from supplied certificates
rm /app/ssl/cacerts 
for crt in `ls -1 $CA_FILEPATH/*.crt`; do
  echo "Found $crt"
  keytool -import -file $crt -alias $crt -keystore /app/ssl/cacerts -storepass password -noprompt
done

java ${NEWRELIC_JVM_FLAG} -jar *-allinone.jar server *.yaml
