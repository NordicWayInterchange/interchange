#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.mock-names=${DNS_MOCK_NAMES} \
     -Ddns.type=${DNS_TYPE} \
     -Dserver.ssl.key-store=/tmp/keys/${SERVER_NAME}.p12\
     -Dserver.ssl.key-store-password=password \
     -Dserver.ssl.key-alias=${SERVER_NAME} \
     -Dserver.ssl.trust-store=/tmp/keys/truststore.jks \
     -Dserver.ssl.trust-store-password=password \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -jar neighbour-server.jar
