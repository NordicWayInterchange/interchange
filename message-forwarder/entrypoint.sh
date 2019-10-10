#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

#TODO need to put more of the settings into env variables.
java -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
     -Dspring.datasource.url=${POSTGRES_URI} \
     -Dforwarder.localIxnDomainName=${SERVER_NAME} \
     -Dforwarder.localIxnFederationPort=5671 \
     -Dforwarder.keystorepath=${KEY_STORE} \
     -Dforwarder.keystorepassword=${KEY_STORE_PASSWORD} \
     -Dforwarder.keystoretype=${KEY_STORE_TYPE} \
     -Dforwarder.truststorepath=${TRUST_STORE} \
     -Dforwarder.truststorepassword=${TRUST_STORE_PASSWORD} \
     -Dforwarder.truststoretype=${TRUST_STORE_TYPE} \
     ${LOG_LEVELS} \
     -jar message-forwarder.jar