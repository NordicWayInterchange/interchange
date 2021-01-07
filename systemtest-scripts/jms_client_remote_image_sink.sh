#!/bin/bash

cd ..
docker run \
    -it \
    --rm \
    --name jms_client_image_sink \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/tmp/keys:/keys \
    -e URL=amqps://remote.bouvetinterchange.eu \
    -e QUEUE=king_gustaf.bouvetinterchange.eu \
    -e KEY_STORE_PATH=/keys/king_gustaf.bouvetinterchange.eu.p12 \
    -e KEY_STORE_PASS=password \
    -e KEY_PASS=password \
    -e TRUST_STORE_PATH=/keys/truststore.jks \
    -e TRUST_STORE_PASS=password \
    --link remote_qpid:remote.bouvetinterchange.eu \
    jms_client_image_sink