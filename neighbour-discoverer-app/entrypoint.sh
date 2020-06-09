#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.domain-name=${DOMAIN_NAME}\
     -Ddns.control-channel-port=${CTRL_CHNL_PORT} \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=pkcs12 \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     -Djavax.net.ssl.keyPassword=${KEY_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     ${LOG_LEVELS} \
     -jar neighbour-discoverer.jar
