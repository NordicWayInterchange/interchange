#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Drouting-configurer.baseUrl=${BASE_URL} \
     -Drouting-configurer.vhost=${BROKER_EXTERNAL_NAME} \
     -Dspring.ssl.bundle.jks.qpid-client.truststore.location=${TRUST_STORE} \
     -Dspring.ssl.bundle.jks.qpid-client.keystore.password=${TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.qpid-client.keystore.location=${KEY_STORE} \
     -Dspring.ssl.bundle.jks.qpid-client.keystore.password=${KEY_STORE_PASSWORD} \
     -Dinterchange.node-provider.brokerExternalName=${BROKER_EXTERNAL_NAME} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     ${LOG_LEVELS} \
     -jar routing-configurer.jar
