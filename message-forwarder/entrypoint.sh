#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

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
-Dforwarder.keystorepath=/tmp/keys/server.p12 \
-Dforwarder.keystorepassword=password \
-Dforwarder.keystoretype=PKCS12 \
-Dforwarder.truststorepath=/tmp/keys/truststore.jks \
-Dforwarder.truststorepassword=password \
-Dforwarder.truststoretype=JKS \
-jar message-forwarder.jar