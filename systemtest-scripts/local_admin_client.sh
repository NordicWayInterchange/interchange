#!/bin/bash

docker run \
  -it \
  --rm \
  --name local_admin_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -e KEY_STORE=/keys/local.bouvetinterchange.eu.p12 \
  -e KEY_STORE_PASSWORD=password \
  -e KEY_PASSWORD=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASSWORD=password \
  -e ADMIN_SERVER=https://local.bouvetinterchange.eu:8799/ \
  -e USER=local.bouvetinterchange.eu \
  --link local_admin_server:local.bouvetinterchange.eu \
  --entrypoint bash admin_rest_client
