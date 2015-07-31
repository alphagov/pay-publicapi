#!/bin/bash

COMMIT=$(git rev-parse HEAD)

if [[ `git status --porcelain` ]]; then
    >&2 echo "Not building - local changes found :("
    exit 1
fi

IMAGE="govukpay/broker:$COMMIT"
mvn clean package && docker build -t $IMAGE . && docker push $IMAGE