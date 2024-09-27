#!/bin/bash

SERVICE_PROVIDER="king_gustaf.bouvetinterchange.eu"
CONTAINER=""
URL=""

if [[ "$#" -ge 1  && $1 == 'messages' ]]; then
CONTAINER="b-qpid"
URL="amqps://b.bouvetinterchange.eu"

else
CONTAINER="b-onboard-server"
URL="https://b.bouvetinterchange.eu:8696/"
fi


docker run \
  -it \
  --rm \
  --name b_service_provider_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link ${CONTAINER}:b.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
