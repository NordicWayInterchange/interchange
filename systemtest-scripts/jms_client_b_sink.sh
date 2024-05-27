#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWD}:/work \
    -e URL=amqps://b.bouvetinterchange.eu \
    -e QUEUE=${1} \
    -e KEY_STORE_PATH=/keys/king_gustaf.bouvetinterchange.eu.p12 \
    -e KEY_STORE_PASS=6_UAQBiKVl-9 \
    -e TRUST_STORE_PATH=/keys/ca.bouvetinterchange.eu.jks \
    -e TRUST_STORE_PASS=ma9+jz78gHHy \
    --link b_qpid:b.bouvetinterchange.eu \
    jms_client_sink
