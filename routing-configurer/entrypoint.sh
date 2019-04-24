#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
-Dqpid.rest.api.baseUrl=${BASE_URL} \
-Dqpid.rest.api.vhost=${VHOST_NAME} \
-Dqpid.rest.api.truststore=${TRUSTSTORE_NAME} \
-Dqpid.rest.api.truststore.password=${TRUSTSTORE_PASSWORD} \
-Dqpid.rest.api.keystore=${KEYSTORE_NAME} \
-Dqpid.rest.api.keystore.password=${KEYSTORE_PASSWORD} \
-Dqpid.rest.api.keystore.key.password=${KEY_PASSWORD} \
    -jar routing-configurer.jar
