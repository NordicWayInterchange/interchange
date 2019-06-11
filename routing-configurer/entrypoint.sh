#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
-Dqpid.rest.api.baseUrl=${BASE_URL} \
-Dqpid.rest.api.vhost=${VHOST_NAME} \
-Drouting-configurer.ssl.trust-store=${TRUSTSTORE_NAME} \
-Drouting-configurer.ssl.trust-store-password=${TRUSTSTORE_PASSWORD} \
-Drouting-configurer.ssl.key-store=${KEYSTORE_NAME} \
-Drouting-configurer.ssl.key-store-password=${KEYSTORE_PASSWORD} \
-Drouting-configurer.ssl.key-password=${KEY_PASSWORD} \
    -jar routing-configurer.jar
