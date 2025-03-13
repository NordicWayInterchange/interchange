#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dspring.datasource.username=${POSTGRES_USER} \
     -Dspring.datasource.password=${POSTGRES_PASSWORD} \
     -Dserver.ssl.bundle="controller" \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -Dadmin.name=${ADMIN_NAME} \
     -Drouting-configurer.baseUrl=${BASE_URL} \
     -Drouting-configurer.vhost=${BROKER_EXTERNAL_NAME} \
     -Dserver.port=${SP_CHNL_PORT} \
     ${LOG_LEVELS} \
     -jar admin-server.jar