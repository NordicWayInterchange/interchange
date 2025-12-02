#!/bin/bash -eux

echo "Starting server"

java \
  -Dserverstub.neighbourname=${NEIGHBOUR_NAME} \
  -Dserverstub.hostname=${HOSTNAME} \
  -Dserverstub.broker=${BROKER} \
  -Dserverstub.queue=${QUEUE} \
  -Dserver.ssl.key-store=${KEY_STORE} \
  -Dserver.ssl.key-store-password=${KEY_STORE_PASSWORD} \
  -Dserver.ssl.trust-store=${TRUST_STORE}\
  -Dserver.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
  -jar /neighbour-server-dynamic-filter-test.jar


