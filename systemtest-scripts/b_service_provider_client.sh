#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
CONTAINER=""
URL=""

if [ $1 == 'messages' ]; then
CONTAINER="b_qpid"
URL="amqps://b.bouvetinterchange.eu"

elif [ $1 == 'capabilities' ] || [ $1 == 'subscriptions' ] || [ $1 == 'deliveries' ] || [ $1 == 'privatechannels' ]; then
CONTAINER="b_onboard_server"
URL="https://b.bouvetinterchange.eu:8696/"
fi


docker run \
  -it \
  --rm \
  --name b_onboard_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link ${CONTAINER}:b.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
