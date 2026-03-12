#!/bin/bash

SERVICE_PROVIDER="king_olav.a.bouvetinterchange.eu"
URL="https://a.bouvetinterchange.eu:8797/"

docker run \
  -it \
  --rm \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v systemtest-keys-volume:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.bouvetinterchange.eu \
  --link a_qpid:a.qpid.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
