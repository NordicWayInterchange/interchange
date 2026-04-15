#!/bin/bash

SERVICE_PROVIDER="king_olav.a.bouvetinterchange.eu"

docker run \
  -it \
  --rm \
  --name a_napcore_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v systemtest-keys-volume:/keys \
  -v $PWD:/work \
  --link a_napcore_server:a.bouvetinterchange.eu \
  napcore_rest_client -k /keys/nap.a.internal.bouvetinterchange.eu.p12 -s password -t /keys/ca.internal.a.bouvetinterchange.eu.jks -w password https://a-napcore-server:8898/ ${SERVICE_PROVIDER} nap.a.internal.bouvetinterchange.eu "$@"
