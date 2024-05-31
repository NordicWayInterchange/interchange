#!/bin/bash

if [ "$#" -gt 0 ]; then
  SERVICE_PROVIDER=$1
else
  SERVICE_PROVIDER="king_gustaf.bouvetinterchange.eu"
fi

docker run \
  -it \
  --rm \
  --name b_onboard_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -e KEY_STORE=/keys/${SERVICE_PROVIDER}.p12 \
  -e KEY_STORE_PASSWORD=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASSWORD=password \
  -e ONBOARD_SERVER=https://b.bouvetinterchange.eu:8696/ \
  -e USER=${SERVICE_PROVIDER} \
  --link b_onboard_server:b.bouvetinterchange.eu \
  --entrypoint bash onboard_rest_client
