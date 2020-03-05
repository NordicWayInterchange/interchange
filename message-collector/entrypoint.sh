#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
     -Dspring.datasource.url=${POSTGRES_URI} \
     -Dcollector.localIxnDomainName=${SERVER_NAME} \
     -Dcollector.localIxnFederationPort=5671 \
     -Dcollector.keystorepath=${KEY_STORE} \
     -Dcollector.keystorepassword=${KEY_STORE_PASSWORD} \
     -Dcollector.keystoretype=${KEY_STORE_TYPE} \
     -Dcollector.truststorepath=${TRUST_STORE} \
     -Dcollector.truststorepassword=${TRUST_STORE_PASSWORD} \
     -Dcollector.truststoretype=${TRUST_STORE_TYPE} \
     ${LOG_LEVELS} \
     -jar message-collector.jar