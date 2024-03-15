#!/bin/bash

docker run \
  -it \
  --rm \
  --name a_napcore_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -e KEY_STORE=/keys/nap.bouvetinterchange.eu.p12 \
  -e KEY_STORE_PASSWORD=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASSWORD=password \
  -e NAP_SERVER=https://a.bouvetinterchange.eu:8898/ \
  -e USER=king_olav.bouvetinterchange.eu \
  -e NAP=nap.bouvetinterchange.eu \
  --link a_napcore_server:a.bouvetinterchange.eu \
  --entrypoint bash napcore_rest_client