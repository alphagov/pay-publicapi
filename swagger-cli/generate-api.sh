#!/usr/bin/env bash

java -jar -Dapis swagger-cli/swagger-codegen-cli-2.2.1.jar generate -i swagger/swagger.json -l jaxrs \
--api-package uk.gov.pay.api.resources --model-package uk.gov.pay.api.model --template-dir swagger-cli/templates -o swagger-cli

cp swagger-cli/src/gen/java/uk/gov/pay/api/resources/V1Api.java src/main/java/uk/gov/pay/api/resources/V1Api.java

rm -rf swagger-cli/src
