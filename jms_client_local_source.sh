#!/bin/bash
docker run \
    -it \
    --rm \
    --name jms_client_source \
    --network=interchange_testing_net \
    --dns=172.28.1.1 \
    -v ${PWD}/tmp/keys:/keys \
    --link local_qpid:local.bouvetinterchange.eu \
    jms_client_source
