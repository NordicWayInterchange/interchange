#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dserver.port=${NAP_CHNL_PORT} \
     -Dserver.ssl.key-store=${KEY_STORE}\
     -Dserver.ssl.key-store-password=${KEY_STORE_PASSWORD} \
     -Dserver.ssl.key-alias=${SERVER_NAME} \
     -Dserver.ssl.trust-store=${TRUST_STORE}\
     -Dserver.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
     -Dnapcore.node-provider.name=${SERVER_NAME} \
     -Dnapcore.node-provider.nap=${NAP_NAME} \
     -Dnapcore.cert-signer.keystore-location=${SIGN_STORE} \
     -Dnapcore.cert-signer.keystore-password=${SIGN_STORE_PASSWORD} \
     -Dnapcore.cert-signer.key-alias=${SIGN_STORE_ALIAS} \
     ${LOG_LEVELS} \
     -jar napcore-server.jar