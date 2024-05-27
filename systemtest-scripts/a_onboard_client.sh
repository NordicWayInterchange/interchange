#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"

docker run \
  -it \
  --rm \
  --name a_onboard_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.bouvetinterchange.eu \
  onboard_rest_client -k /keys/${SERVICE_PROVIDER}.p12 -s Pi+TS1I17pNK -t /keys/ca.bouvetinterchange.eu.jks -w ma9+jz78gHHy https://a.bouvetinterchange.eu:8797/ ${SERVICE_PROVIDER} "$@"
