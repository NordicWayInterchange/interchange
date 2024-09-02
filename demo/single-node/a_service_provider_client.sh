#!/bin/bash

SERVICE_PROVIDER="king_olav.interchangedomain.com"
URL=""

if [ $1 == 'messages' ]; then
URL="amqps://a.qpid.interchangedomain.com"
else
URL="https://a.interchangedomain.com:8797/"
fi

docker run \
  -it \
  --rm \
  --name a_service_provider_client${RANDOM} \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v $PWD/../keys/a:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.interchangedomain.com \
  --link a_qpid:a.qpid.interchangedomain.com \
  europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/service-provider-client:$(<version) -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password $URL -u ${SERVICE_PROVIDER} "$@"
