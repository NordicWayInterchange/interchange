#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
CONTAINER=""
URL=""


if [ $1 == 'messages' ]; then
CONTAINER="a_qpid"
URL="amqps://a.bouvetinterchange.eu"

else
CONTAINER="a_onboard_server"
URL="https://a.bouvetinterchange.eu:8797/"
fi


docker run \
  -it \
  --rm \
  --name a_service_provider_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link ${CONTAINER}:a.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
