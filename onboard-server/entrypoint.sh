#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dserver.ssl.key-store=${KEY_STORE}\
     -Dserver.port=${SP_CHNL_PORT} \
     -Dserver.ssl.key-store-password=${KEY_STORE_PASSWORD} \
     -Dserver.ssl.key-alias=${SERVER_NAME} \
     -Dserver.ssl.trust-store=${TRUST_STORE}\
     -Dserver.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     ${LOG_LEVELS} \
     -jar onboard-server.jar
