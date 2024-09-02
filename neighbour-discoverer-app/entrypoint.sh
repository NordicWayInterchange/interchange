#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Ddns.domain-name=${DOMAIN_NAME}\
     -Djdk.tls.client.protocols="TLSv1.3" \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=pkcs12 \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dinterchange.node-provider.brokerExternalName=${BROKER_EXTERNAL_NAME} \
     ${LOG_LEVELS} \
     -jar neighbour-discoverer.jar
