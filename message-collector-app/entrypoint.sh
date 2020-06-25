#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
     -Dspring.datasource.url=${POSTGRES_URI} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dcollector.localIxnFederationPort=5671 \
     -Djavax.net.ssl.trustStore=${TRUST_STORE} \
     -Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD} \
     -Djavax.net.ssl.trustStoreType=${TRUST_STORE_TYPE} \
     -Djavax.net.ssl.keyStore=${KEY_STORE} \
     -Djavax.net.ssl.keyStoreType=${KEY_STORE_TYPE} \
     -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
     ${LOG_LEVELS} \
     -jar message-collector.jar