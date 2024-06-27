#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"

docker run \
  -it \
  --rm \
  --name a_napcore_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link a_napcore_server:a.bouvetinterchange.eu \
  napcore_rest_client -k /keys/nap.bouvetinterchange.eu.p12 -s password -t /keys/truststore.jks -w password https://a.bouvetinterchange.eu:8898/ ${SERVICE_PROVIDER} nap.bouvetinterchange.eu "$@"
