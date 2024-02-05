#!/usr/bin/env bash
set -euo pipefail

java \
    -Dsink.url=${URL} \
    -Dsink.receiveQueue=${QUEUE} \
    -Dsink.keystorePath=${KEY_STORE_PATH} \
    -Dsink.keystorepass=${KEY_STORE_PASS} \
    -Dsink.truststorepath=${TRUST_STORE_PATH} \
    -Dsink.truststorepass=${TRUST_STORE_PASS} \
    -jar jms-client-image-sink.jar

