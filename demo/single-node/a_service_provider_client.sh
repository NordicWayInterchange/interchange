#!/bin/bash

SERVICE_PROVIDER="a.interchangedomain.lookupdomain.king_olav@slottet.no"
URL="https://a.interchangedomain.com:8797/"
VOLUME_NAME=single-node-keys-volume

docker run \
  -it \
  --rm \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v ${VOLUME_NAME}:/keys \
  -v ${PWD}:/work \
  --link a-onboard-server:a.interchangedomain.com \
  --link a-qpid:a.qpid.interchangedomain.com \
  ghcr.io/nordicwayinterchange/service-provider-client:$(<version) -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password ${URL} -u ${SERVICE_PROVIDER} "$@"
