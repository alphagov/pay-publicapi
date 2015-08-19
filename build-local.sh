#!/bin/bash
mvn -DskipTests package && docker build -t govukpay/publicapi:local .