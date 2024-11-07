#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
CONTAINER=""
URL=""

if [ $1 == 'messages' ]; then
CONTAINER="c_qpid"
URL="amqps://c.bouvetinterchange.eu"
fi

else
CONTAINER="c_onboard_server"
URL="https://c.bouvetinterchange.eu:8595/"
fi


docker run \
  -it \
  --rm \
  --name c_service_provider_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link ${CONTAINER}:b.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
