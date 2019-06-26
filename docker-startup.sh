#!/usr/bin/env bash

set -eu

if [ -n "${CERTS_PATH:-}" ]; then
  i=0
  truststore_pass=changeit
  for cert in "$CERTS_PATH"/*; do
    [ -f "$cert" ] || continue
    echo "Adding $cert to default truststore"
    keytool -importcert -noprompt -cacerts -storepass "$truststore_pass" -file "$cert" -alias custom$((i++))
  done
fi

exec java ${JAVA_OPTS} -jar *-allinone.jar server *.yaml
