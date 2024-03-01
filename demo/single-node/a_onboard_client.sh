#!/bin/bash

SERVICE_PROVIDER="king_olav.a.interchangedomain.com"

docker run \
  -it \
  --rm \
  --name a_onboard_client \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v $PWD/../keys/a:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.interchangedomain.com \
   europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/onboard-rest-client:6a8a722 -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/truststore.jks -w password https://a.interchangedomain.com:8797/ ${SERVICE_PROVIDER} "$@"
