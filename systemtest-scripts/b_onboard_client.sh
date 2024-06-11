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
  --link b-onboard-server:b.bouvetinterchange.eu \
  onboard_rest_client -k /keys/${SERVICE_PROVIDER}.p12 -s 2v78NcXLRkh1 -t /keys/ca.bouvetinterchange.eu.jks  -w dRnrxu3pSKA6 https://b.bouvetinterchange.eu:8696/ ${SERVICE_PROVIDER} "$@"
