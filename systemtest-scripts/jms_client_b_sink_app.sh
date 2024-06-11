#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_sink_app \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWD}:/work \
    --link b-qpid:b.bouvetinterchange.eu \
    jms_client_sink_app amqps://b.bouvetinterchange.eu -k /keys/king_gustaf.bouvetinterchange.eu.p12 -s 2v78NcXLRkh1 -t /keys/ca.bouvetinterchange.eu.jks -w dRnrxu3pSKA6 "$@"
