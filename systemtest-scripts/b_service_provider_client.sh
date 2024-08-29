#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
URL=""

if [ $1 == 'messages' ] && [ $2 != 'listen' ]; then
URL="amqps://b.qpid.bouvetinterchange.eu"

else
URL="https://b.bouvetinterchange.eu:8696/"
fi


docker run \
  -it \
  --rm \
  --name b_service_provider_client${RANDOM} \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link b_onboard_server:b.bouvetinterchange.eu \
  --link b_qpid:b.qpid.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
