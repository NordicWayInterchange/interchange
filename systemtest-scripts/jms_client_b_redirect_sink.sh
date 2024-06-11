#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink_app \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWD}:/work \
    --link a-qpid:a.bouvetinterchange.eu \
    jms_client_sink_app amqps://a.bouvetinterchange.eu -k /keys/king_gustaf.bouvetinterchange.eu.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks -w password "$@"
