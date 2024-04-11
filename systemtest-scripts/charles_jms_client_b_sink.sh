#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    --link b_qpid:b.bouvetinterchange.eu \
    jms_client_sink_app -k /keys/king_charles.bouvetinterchange.eu.p12 -s password -t /keys/truststore.jks -w password amqps://b.bouvetinterchange.eu receivemessages ${1}
