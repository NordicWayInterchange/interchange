#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dqpid.rest.api.baseUrl=${BASE_URL} \
     -Dqpid.rest.api.vhost=${SERVER_NAME} \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=JKS \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=pkcs12 \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     -Djavax.net.ssl.keyPassword=${KEY_PASSWORD} \
     ${LOG_LEVELS} \
     -jar routing-configurer.jar
