#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
     -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dcollector.localIxnFederationPort=5671 \
     -Dspring.ssl.bundle.jks.external-service.truststore.location=${TRUST_STORE} \
     -Dspring.ssl.bundle.jks.external-service.truststore.password=${TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.external-service.truststore.type=${TRUST_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.location=${KEY_STORE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.type=${KEY_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.password=${KEY_STORE_PASSWORD} \
     ${LOG_LEVELS} \
     -jar message-collector.jar