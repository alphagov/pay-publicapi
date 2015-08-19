#!/bin/bash

#!/bin/bash
if [ "x${ghprbActualCommit}" = "x" ]; then
    COMMIT=$(git rev-parse HEAD)
else
    COMMIT=$ghprbActualCommit
fi

IMAGE="govukpay/publicapi:$COMMIT"
mvn clean package && docker build -t $IMAGE . && docker push $IMAGE