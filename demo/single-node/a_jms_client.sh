#!/bin/bash

docker run \
    -it \
    --rm \
    --network=single-node_singletest \
    --dns=172.28.1.1 \
    -v ${PWD}/../keys/a:/keys \
    -v ${PWD}:/work \
    -e URL=amqps://a.interchangedomain.com \
    -e QUEUE=${1} \
    -e KEY_STORE_PATH=/keys/king_olav.interchangedomain.com.p12 \
    -e KEY_STORE_PASS=password \
    -e TRUST_STORE_PATH=/keys/truststore.jks \
    -e TRUST_STORE_PASS=password \
    --link a_qpid:a.interchangedomain.com \
    europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/jms-client-app:$(<version) -k /keys/king_olav.interchangedomain.com.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password amqps://a.interchangedomain.com "$@"
