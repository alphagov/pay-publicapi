#!/usr/bin/env bash

#create keystore and import certs if necessary properties present
function configure_keystore {
    if [ -z "${CERTS_DIR}" -a \
         -z "${KEYSTORE_DIR}" -a \
         -z "${KEYSTORE_PASSWORD}" -a \
         -z "${KEYSTORE_FILE}" -a \
         -z "${CERT_FILE}" -a \
         -z "${KEY_FILE}" ];
         then
           echo "not all variables needed for keystore creation are available";
         else
           if [ -d "${CERTS_DIR}" -a -d "${KEYSTORE_DIR}" ];
           then
                #replace correct filenames
                openssl pkcs12 -export -out ${KEYSTORE_DIR}/${KEYSTORE_FILE} -inkey ${CERTS_DIR}/${KEY_FILE} -in ${CERTS_DIR}/${CERT_FILE} -passout pass:${KEYSTORE_PASSWORD};
                if [ $? -eq 0 ]; then
                    echo "keystore created successfully at \"${KEYSTORE_DIR}/${KEYSTORE_FILE}\" ";
                else
                    echo "keystore creation failed";
                fi
           else
                echo "KEYSTORE_DIR and CERTS_DIR does not exist";
           fi
    fi
}

if [ "${ENABLE_NEWRELIC}" == "yes" ]; then
  NEWRELIC_JVM_FLAG="-javaagent:/app/newrelic/newrelic.jar"
fi

## TODO enable this after figuring out the failure during `openssl` above
# configure_keystore
java ${NEWRELIC_JVM_FLAG} -jar *-allinone.jar server *.yaml
