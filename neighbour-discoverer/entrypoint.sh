#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.type=${DNS_TYPE} \
     -Ddns.domain-name=${DOMAIN_NAME}\
     -Ddns.control-channel-port=${CTRL_CHNL_PORT} \
     -Dneighbour.ssl.trust-store-password=password \
     -Dneighbour.ssl.trust-store=/jks/keys/truststore.jks \
     -Dneighbour.ssl.key-store=/jks/keys/${SERVER_NAME}.p12 \
     -Dneighbour.ssl.key-store-password=password \
     -Dneighbour.ssl.key-password=password \
     -Dinterchange.node-provider.name=${NODE_PROVIDER_NAME} \
     -jar neighbour-discoverer.jar
