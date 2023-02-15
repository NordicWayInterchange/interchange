#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_source \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -e URL=amqps://c.bouvetinterchange.eu \
    -e QUEUE=${1} \
    -e KEY_STORE_PATH=/keys/king_charles.bouvetinterchange.eu.p12 \
    -e KEY_STORE_PASS=password \
    -e TRUST_STORE_PATH=/keys/truststore.jks \
    -e TRUST_STORE_PASS=password \
    --link c_qpid:c.bouvetinterchange.eu \
    jms_client_source