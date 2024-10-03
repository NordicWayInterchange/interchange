#!/bin/bash

SERVICE_PROVIDER="king_olav.interchangedomain.com"
CONTAINER=""
URL=""

if [[ "$#" -ge 1 && $1 == 'messages' ]]; then
CONTAINER="a-qpid"
URL="amqps://a.interchangedomain.com"

else
CONTAINER="a_onboard_server"
URL="https://a.interchangedomain.com:8797/"
fi

docker run \
  -it \
  --rm \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v $PWD/../keys/a:/keys \
  -v $PWD:/work \
  --link ${CONTAINER}:a.interchangedomain.com \
  europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/service-provider-client:$(<version) -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password $URL -u ${SERVICE_PROVIDER} "$@"
