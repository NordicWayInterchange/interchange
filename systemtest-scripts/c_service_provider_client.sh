#!/bin/bash

SERVICE_PROVIDER="king_olav.bouvetinterchange.eu"
URL=""

if [ $1 == 'messages' ]; then
URL="amqps://c.qpid.bouvetinterchange.eu"

else 
URL="https://c.bouvetinterchange.eu:8595/"
fi


docker run \
  -it \
  --rm \
  --name b_service_provider_client \
  --network=systemtest-scripts_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  --link c_onboard_server:c.bouvetinterchange.eu \
  --link c_qpid:c.qpid.bouvetinterchange.eu
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password $URL -u ${SERVICE_PROVIDER} "$@"
