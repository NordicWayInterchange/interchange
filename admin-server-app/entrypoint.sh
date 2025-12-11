#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dspring.ssl.bundle.jks.qpid.keystore.location=${KEY_STORE} \
     -Dspring.ssl.bundle.jks.qpid.keystore.password=${KEY_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.qpid.keystore.type=PKCS12 \
     -Dspring.ssl.bundle.jks.qpid.truststore.location=${TRUST_STORE} \
     -Dspring.ssl.bundle.jks.qpid.truststore.password=${TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.qpid.truststore.type=JKS \
     -Dspring.ssl.bundle.jks.controller.keystore.location=${KEY_STORE} \
     -Dspring.ssl.bundle.jks.controller.keystore.password=${KEY_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.controller.keystore.type=PKCS12 \
     -Dspring.ssl.bundle.jks.controller.truststore.location=${TRUST_STORE} \
     -Dspring.ssl.bundle.jks.controller.truststore.password=${TRUST_STORE_PASSWORD} \
     -Dspring.ssl.bundle.jks.controller.truststore.type=JKS \
     -Dserver.ssl.bundle="controller" \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dadmin.name=${ADMIN_NAME} \
     -Dadmin.brokerexternalname=${BROKER_EXTERNAL_NAME} \
     -Dadmin.qpid.client.baseUrl=${BASE_URL} \
     -Dadmin.qpid.client.vhost=${BROKER_EXTERNAL_NAME} \
     -Dserver.port=${SP_CHNL_PORT} \
     ${LOG_LEVELS} \
     -jar admin-server.jar