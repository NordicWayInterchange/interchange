#!/bin/bash

SERVICE_PROVIDER="king_gustaf.b.bouvetinterchange.eu"
URL="https://b.bouvetinterchange.eu:8696/"


docker run \
  -it \
  --rm \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v systemtest-keys-volume:/keys \
  -v $PWD:/work \
  --link b_onboard_server:b.bouvetinterchange.eu \
  --link b_qpid:b.qpid.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
