#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    --link a_qpid:a.bouvetinterchange.eu \
    jms_client_sink_app -k /keys/king_olav.bouvetinterchange.eu.p12 -s NJfOF7VxznOe -t /keys/truststore.jks -w ma9+jz78gHHy amqps://a.bouvetinterchange.eu receivemessages $1
