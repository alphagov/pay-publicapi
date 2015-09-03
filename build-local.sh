#!/bin/bash
mvn -DskipTests clean package && docker build -t govukpay/publicapi:local .
