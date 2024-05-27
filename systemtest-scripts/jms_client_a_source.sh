#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_source \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWD}:/work \
    --link a_qpid:a.bouvetinterchange.eu \
    jms_client_source_app -k /keys/king_olav.bouvetinterchange.eu.p12 -s Pi+TS1I17pNK -t /keys/ca.bouvetinterchange.eu.jks -w  ma9+jz78gHHy amqps://a.bouvetinterchange.eu "$@" #sendpredefinedmessage $1
