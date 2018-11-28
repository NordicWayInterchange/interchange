#!/bin/bash -eu

if [[ ! -d ./debugclient/target ]]; then
    echo "ERROR - debug client not found. Have you built it?"
    exit 1
fi

SERVER=${1:-localhost}

SECURE_DIR="./tmp/keys"

KEYSTORE_FILE="${SECURE_DIR}/guest.p12"
KEYSTORE_PASSWORD=password
TRUSTSTORE_FILE="${SECURE_DIR}/truststore.jks"
TRUSTSTORE_PASSWORD=password
CLIENT_JAR=debugclient/target/debugclient-1.0-SNAPSHOT-jar-with-dependencies.jar
SERVER_URI="amqps://${SERVER}:5671"
SEND_QUEUE=nwEx
RECEIVE_QUEUE=test-out

java \
    -Djavax.net.ssl.keyStore=${KEYSTORE_FILE} \
    -Djavax.net.ssl.keyStorePassword=${KEYSTORE_PASSWORD} \
    -Djavax.net.ssl.keyStoreType=pkcs12 \
    -Djavax.net.ssl.trustStore=${TRUSTSTORE_FILE} \
    -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD} \
    -jar ${CLIENT_JAR} \
    ${SERVER_URI} ${SEND_QUEUE} ${RECEIVE_QUEUE}
