#!/bin/bash

docker run \
    -it \
    --rm \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWN}:/work
    --link b_qpid:b.bouvetinterchange.eu \
    jms_client -k /keys/king_gustaf.bouvetinterchange.eu.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks -w password amqps://b.bouvetinterchange.eu "$@"
