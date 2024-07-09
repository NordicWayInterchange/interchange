#!/bin/bash

docker run \
    -it \
    --rm \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    -v ${PWD}:/work \
    --link a_qpid:a.bouvetinterchange.eu \
    jms_client -k /keys/king_olav.bouvetinterchange.eu.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks -w password amqps://a.bouvetinterchange.eu "$@"
