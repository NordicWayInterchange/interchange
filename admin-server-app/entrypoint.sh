#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=pkcs12 \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     -Dserver.ssl.key-store=${KEY_STORE}\
     -Dserver.ssl.key-store-password=${KEY_STORE_PASSWORD} \
     -Dserver.ssl.key-alias=${SERVER_NAME} \
     -Dserver.ssl.trust-store=${TRUST_STORE}\
     -Dserver.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dadmin.name=${ADMIN_NAME} \
     -Drouting-configurer.baseUrl=${BASE_URL} \
     -Drouting-configurer.vhost=${BROKER_EXTERNAL_NAME} \
     -Dserver.port=${SP_CHNL_PORT} \
     ${LOG_LEVELS} \
     -jar admin-server.jar