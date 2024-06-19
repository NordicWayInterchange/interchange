#!/bin/bash -eu

SERVICE_PROVIDER="king_olav.a.interchangedomain.com"
IXN_VERSION=$(<version)


docker run \
  -it \
  --rm \
  --name a_onboard_client \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v $PWD/../keys/a:/keys \
  -v $PWD:/work \
  --link a_onboard_server:a.interchangedomain.com \
   europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/onboard-rest-client:${IXN_VERSION} -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.lookupdomain.com.jks -w password https://a.interchangedomain.com:8797/ ${SERVICE_PROVIDER} "$@"
