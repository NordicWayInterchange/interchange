#!/bin/bash
set -euo pipefail

java \
    -Dsource.url=${URL} \
    -Dsource.sendQueue=${QUEUE} \
    -Dsource.keyStorePath=${KEY_STORE_PATH} \
    -Dsource.keyStorepass=${KEY_STORE_PASS} \
    -Dsource.keypass=${KEY_PASS} \
    -Dsource.trustStorepath=${TRUST_STORE_PATH} \
    -Dsource.trustStorepass=${TRUST_STORE_PASS} \
    -jar jms-client-source.jar

