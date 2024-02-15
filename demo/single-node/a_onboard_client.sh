#!/bin/bash

#if [ "$#" -gt 0 ]; then
#  SERVICE_PROVIDER=$1
#else
  SERVICE_PROVIDER="king_olav.a.interchangedomain.com"
#fi

docker run \
  -it \
  --rm \
  --name a_onboard_client \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v $PWD/../tmp/keys:/keys \
  -v $PWD:/work \
  -e KEY_STORE=/keys/${SERVICE_PROVIDER}.p12 \
  -e KEY_STORE_PASSWORD=password \
  -e TRUST_STORE_PATH=/keys/truststore.jks \
  -e TRUST_STORE_PASSWORD=password \
  -e ONBOARD_SERVER=https://a.interchangedomain.com:8797/ \
  -e USER=${SERVICE_PROVIDER} \
  --link a_onboard_server:a.interchangedomain.com \
  onboard-rest-client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/truststore.jks -w password https://a.interchangedomain.com:8797/ ${SERVICE_PROVIDER}  "$@"
