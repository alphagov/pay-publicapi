#!/usr/bin/env bash

set -eu

if [ -n "${CERTS_PATH:-}" ]; then
  i=0
  truststore=/etc/ssl/certs/java/cacerts
  truststore_pass=changeit
  existing_fingerprints=$(keytool -list -keystore "$truststore" -storepass "$truststore_pass"| sed -ne 's/^Certificate fingerprint (SHA1): //p')
  for cert in "$CERTS_PATH"/*; do
    [ -f "$cert" ] || continue
    if grep -qFx "$(openssl x509 -in "$cert" -fingerprint -noout | sed -ne 's/^SHA1 Fingerprint=//p')" <<<"$existing_fingerprints"; then
      echo "$cert already in truststore $truststore"
    else
      echo "Adding $cert to $truststore"
      keytool -importcert -noprompt -keystore "$truststore" -storepass "$truststore_pass" -file "$cert" -alias custom$((i++))
    fi
  done
fi

java ${JAVA_OPTS} -jar *-allinone.jar server *.yaml
