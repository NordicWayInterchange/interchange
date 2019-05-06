#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java \
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
-Dspring.datasource.url="jdbc:postgresql://localhost:7070/federation" \
-Dspring.datasource.username=federation \
-Dspring.datasource.password=federation \
-Dspring.jpa.hibernate.ddl-auto=create-drop \
-Dlogging.pattern.console="%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n" \
-Dlogging.level.org.hibernate.SQL=debug \
-Dforwarder.localIxnDomainName=bouvet \
-Dforwarder.localIxnFederationPort =5671 \
-Dforwarder.keystorepath=local.p12 \
-Dforwarder.keystorepassword=password \
-Dforwarder.keystoretype=PKCS12 \
-Dforwarder.truststorepath=truststore.jks \
-Dforwarder.truststorepassword=password \
-Dforwarder.truststoretype=JKS \
-jar message-forwarder.jar