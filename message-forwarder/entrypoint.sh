#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

#TODO need to put more of the settings into env variables.
java \
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
-Dspring.datasource.url="jdbc:postgresql://message-forwarder-db:5432/federation" \
-Dspring.datasource.username=federation \
-Dspring.datasource.password=federation \
-Dspring.jpa.hibernate.ddl-auto=create-drop \
-Dlogging.pattern.console='%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n' \
-Dlogging.level.org.hibernate.SQL=debug \
-Dforwarder.localIxnDomainName=bouvet \
-Dforwarder.localIxnFederationPort=5671 \
-Dforwarder.keystorepath="$KEY_STORE_FILE" \
-Dforwarder.keystorepassword="$KEY_STORE_PASSWORD" \
-Dforwarder.keystoretype="$KEY_STORE_TYPE" \
-Dforwarder.truststorepath="$TRUST_STORE_PATH" \
-Dforwarder.truststorepassword="$TRUST_STORE_PASSWORD" \
-Dforwarder.truststoretype="$TRUST_STORE_TYPE" \
-jar message-forwarder.jar