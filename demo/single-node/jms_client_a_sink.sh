#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink \
    --network=single-node_singletest \
    --dns=172.28.1.1 \
    -v ${PWD}/../keys/a:/keys \
    -e URL=amqps://a.interchangedomain.com \
    -e QUEUE=${1} \
    -e KEY_STORE_PATH=/keys/king_olav.a.interchangedomain.com.p12 \
    -e KEY_STORE_PASS=password \
    -e TRUST_STORE_PATH=/keys/truststore.jks \
    -e TRUST_STORE_PASS=password \
    --link a_qpid:a.interchangedomain.com \
    jms_client_sink