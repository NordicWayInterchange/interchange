#!/bin/bash

docker run \
  -it \
  --rm \
  --name jms_client_source_app \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v ${PWD}/../tmp/keys:/keys \
  -e URL=amqps://a.bouvetinterchange.eu \
  -e QUEUE=${1} \
  -e KEY_STORE_PATH=/keys/king_olav.bouvetinterchange.eu.p12 \
  -e KEY_STORE_PASS=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASS=password \
  --link a_qpid:a.bouvetinterchange.eu \
  --entrypoint bash jms_client_source_app
