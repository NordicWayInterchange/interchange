#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
CONTAINER=""
URL=""

if [ $1 == 'messages' ]; then
CONTAINER="c_qpid"
URL="amqps://c.bouvetinterchange.eu"
fi

elif [ $1 == 'capabilities' ] || [ $1 == 'subscriptions' ] || [ $1 == 'deliveries' ] || [ $1 == 'privatechannels' ]; then
CONTAINER="c_onboard_server"
URL="https://c.bouvetinterchange.eu:8595/"
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
