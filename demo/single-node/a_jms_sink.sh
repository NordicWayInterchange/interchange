#!/bin/bash

docker run \
  -it \
  --rm \
  --name jms_client_sink_app \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v ${PWD}/../keys/a:/keys \
  -v ${PWD}:/work \
  --link a_qpid:a.interchangedomain.com \
   europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/jms-client-sink-app:3743e41 -k /keys/king_olav.a.interchangedomain.com.p12  -s password -t /keys/truststore.jks -w password amqps://a.interchangedomain.com "$@"