#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.mock.mock-names=${DNS_MOCK_NAMES} \
     -Ddns.type=${DNS_TYPE} \
     -Dneighbour.ssl.trust-store-password=password \
     -Dneighbour.ssl.trust-store=/jks/keys/truststore.jks \
     -Dneighbour.ssl.key-store=/jks/keys/${SERVER_NAME}.p12 \
     -Dneighbour.ssl.key-store-password=password \
     -Dneighbour.ssl.key-password=password \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -jar neighbour-discoverer.jar
