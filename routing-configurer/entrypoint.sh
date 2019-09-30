#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dqpid.rest.api.baseUrl=${BASE_URL} \
     -Dqpid.rest.api.vhost=${SERVER_NAME} \
     -Drouting-configurer.ssl.trust-store=${TRUST_STORE} \
     -Drouting-configurer.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
     -Drouting-configurer.ssl.key-store=${KEY_STORE} \
     -Drouting-configurer.ssl.key-store-password=${KEY_STORE_PASSWORD} \
     -Drouting-configurer.ssl.key-password=${KEY_PASSWORD} \
     ${LOG_LEVELS} \
     -jar routing-configurer.jar
