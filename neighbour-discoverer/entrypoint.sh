#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.domain-name=${DOMAIN_NAME}\
     -Ddns.control-channel-port=${CTRL_CHNL_PORT} \
     -Dneighbour.ssl.trust-store-password=${TRUST_STORE_PASSWORD} \
     -Dneighbour.ssl.trust-store=${TRUST_STORE} \
     -Dneighbour.ssl.key-store=${KEY_STORE} \
     -Dneighbour.ssl.key-store-password=${KEY_STORE_PASSWORD}\
     -Dneighbour.ssl.key-password=${KEY_PASSWORD} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -jar neighbour-discoverer.jar
