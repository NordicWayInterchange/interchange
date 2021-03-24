#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink \
    --network=private-channel-systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -e URL=amqps://local.bouvetinterchange.eu \
    -e QUEUE=king_gustaf.bouvetinterchange.eu \
    -e KEY_STORE_PATH=/keys/king_gustaf.bouvetinterchange.eu.p12 \
    -e KEY_STORE_PASS=password \
    -e KEY_PASS=password \
    -e TRUST_STORE_PATH=/keys/truststore.jks \
    -e TRUST_STORE_PASS=password \
    --link local_qpid:local.bouvetinterchange.eu \
    jms_client_sink