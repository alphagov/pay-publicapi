#!/usr/bin/env bash

set -eu

if [ -n "${CERTS_PATH:-}" ]; then
  i=0
  truststore=/etc/ssl/certs/java/cacerts
  truststore_pass=changeit
  for cert in "$CERTS_PATH"/*; do
    [ -f "$cert" ] || continue
    echo "Adding $cert to $truststore"
    keytool -importcert -noprompt -keystore "$truststore" -storepass "$truststore_pass" -file "$cert" -alias custom$((i++))
  done
fi

java ${JAVA_OPTS} -jar *-allinone.jar server *.yaml
