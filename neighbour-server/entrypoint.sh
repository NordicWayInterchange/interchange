#!/bin/bash -eu

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddns.type=${DNS_TYPE} \
     -Ddns.domain-name=${DOMAIN_NAME}\
     -Ddns.control-channel-port=${CTRL_CHNL_PORT} \
     -Dserver.ssl.key-store=/jks/keys/${SERVER_NAME}.p12\
     -Dserver.port=${CTRL_CHNL_PORT} \
     -Dserver.ssl.key-store-password=password \
     -Dserver.ssl.key-alias=${SERVER_NAME} \
     -Dserver.ssl.trust-store=/jks/keys/truststore.jks \
     -Dserver.ssl.trust-store-password=password \
     -Dinterchange.node-provider.name=${NODE_PROVIDER_NAME} \
     -jar neighbour-server.jar
