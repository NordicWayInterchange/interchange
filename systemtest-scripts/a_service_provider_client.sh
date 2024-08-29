#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
URL=""

if [ $1 == 'messages' ] && [ $2 != 'listen' ]; then
URL="amqps://a.qpid.bouvetinterchange.eu"

else
URL="https://a.bouvetinterchange.eu:8797/"
fi

docker run \
  -it \
  --rm \
  --name a_onboard_client${RANDOM} \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.bouvetinterchange.eu \
  --link a_qpid:a.qpid.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
