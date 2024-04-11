#!/bin/bash

docker run \
    -it \
    --rm \
    --name jms_client_source \
    --network=systemtest-scripts_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/../tmp/keys:/keys \
    --link a_qpid:a.bouvetinterchange.eu \
    jms_client_source_app -k /keys/king_olav.bouvetinterchange.eu.p12 -s password -t /keys/truststore.jks -w password amqps://a.bouvetinterchange.eu sendpredefinedmessage $1
