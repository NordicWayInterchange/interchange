#!/bin/bash -eu

IXN_VERSION=$(<version)

docker run \
    -it \
    --rm \
    --network=single-node_singletest \
    --dns=172.28.1.1 \
    -v ${PWD}/../keys/a:/keys \
    -v ${PWD}:/work \
    --link a_qpid:a.interchangedomain.com \
    europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/jms-client-app:${IXN_VERSION} -k /keys/king_olav.a.interchangedomain.com.p12 -s password -t /keys/ca.lookupdomain.com.jks -w password amqps://a.interchangedomain.com "$@"
