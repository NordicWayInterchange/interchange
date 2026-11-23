#!/bin/bash

SERVICE_PROVIDER="a.interchangedomain.com.nap.king_olav@slottet.no"
URL="https://a.interchangedomain.com:8797/"

docker run \
  -it \
  --rm \
  --network=systemtest-scripts_testing_net \
  --dns=172.26.1.1 \
  -v single-node-systemtest-keys-volume:/keys \
  -v ${PWD}:/work \
  --link a-onboard-server:a.interchangedomain.com \
  --link a-qpid:a.qpid.interchangedomain.com \
  onboard_rest_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password ${URL} -u ${SERVICE_PROVIDER} "$@"
