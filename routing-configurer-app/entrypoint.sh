#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Drouting-configurer.baseUrl=${BASE_URL} \
     -Drouting-configurer.vhost=${SERVER_NAME} \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=pkcs12 \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Drouting-configurer.collector-user=${COLLECTOR_USER} \
     ${LOG_LEVELS} \
     -jar routing-configurer.jar
