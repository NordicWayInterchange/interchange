#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}
LOCAL_IXN_PORT=${LOCAL_IXN_PORT:-5671}
java -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
     -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dcollector.localIxnFederationPort=${LOCAL_IXN_PORT} \
     -Dspring.ssl.bundle.jks.external-service.truststore.location=${TRUST_STORE} \
     -Dspring.ssl.bundle.jks.external-service.truststore.password=${TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.external-service.truststore.type=${TRUST_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.location=${KEY_STORE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.type=${KEY_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.external-service.keyStore.password=${KEY_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.internal-service.truststore.location=${INTERNAL_TRUST_STORE} \
     -Dspring.ssl.bundle.jks.internal-service.truststore.password=${INTERNAL_TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.internal-service.truststore.type=${INTERNAL_TRUST_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.internal-service.keyStore.location=${INTERNAL_KEY_STORE} \
     -Dspring.ssl.bundle.jks.internal-service.keyStore.type=${INTERNAL_KEY_STORE_TYPE} \
     -Dspring.ssl.bundle.jks.internal-service.keyStore.password=${INTERNAL_KEY_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.internal-service.key.alias=${INTERNAL_KEY_STORE_ALIAS} \
     ${LOG_LEVELS} \
     -jar message-collector.jar