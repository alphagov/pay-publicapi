#!/usr/bin/env bash

java -jar -Dapis -Dmodels swagger-cli/swagger-codegen-cli-2.3.1.jar generate -i swagger/swagger.json -l jaxrs \
--api-package uk.gov.pay.api.resources --model-package uk.gov.pay.api.model.generated --template-dir swagger-cli/templates -o swagger-cli

mkdir -p src/main/java/uk/gov/pay/api/model/generated/

cp swagger-cli/src/gen/java/uk/gov/pay/api/resources/V1Api.java src/main/java/uk/gov/pay/api/resources/V1Api.java
cp swagger-cli/src/gen/java/uk/gov/pay/api/model/generated/* src/main/java/uk/gov/pay/api/model/generated/

rm -rf swagger-cli/src
