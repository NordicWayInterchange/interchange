#!/bin/bash

docker run \
  -it \
  --rm \
  --name jms_client_source_app \
  --network=single-node_singletest \
  --dns=172.28.1.1 \
  -v ${PWD}/../keys/a:/keys \
  -v ${PWD}:/work \
  --link a_qpid:a.interchangedomain.com \
   europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/jms-client-source-app:5484e5e -k /keys/king_gustaf.interchangedomain.com.p12  -s password -t /keys/a.interchangedomain.com.jks -w password amqps://a.interchangedomain.com "$@"
