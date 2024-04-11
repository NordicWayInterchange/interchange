#!/bin/bash

  SERVICE_PROVIDER="king_gustaf.bouvetinterchange.eu"

docker run \
  -it \
  --rm \
  --name b_onboard_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link b_onboard_server:b.bouvetinterchange.eu \
  onboard_rest_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/truststore.jks  -w password https://b.bouvetinterchange.eu:8696/ ${SERVICE_PROVIDER} "$@"
