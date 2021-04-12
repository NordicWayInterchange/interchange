#!/bin/bash

docker run \
  -it \
  --rm \
  --name remote_onboard_client \
  --network=private-channel-systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -e KEY_STORE=/keys/king_gustaf.bouvetinterchange.eu.p12 \
  -e KEY_STORE_PASSWORD=password \
  -e KEY_PASSWORD=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASSWORD=password \
  -e ONBOARD_SERVER=https://local.bouvetinterchange.eu:8696/ \
  -e USER=king_gustaf.bouvetinterchange.eu \
  --link local_onboard_server:local.bouvetinterchange.eu \
  --entrypoint bash onboard_rest_client
