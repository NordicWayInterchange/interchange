#!/bin/bash -eu

if [[ ! -d ./debugclient/target ]]; then
    echo "ERROR - debug client not found. Have you built it?"
    exit 1
fi

SERVER=${1:-localhost}

KEYSTORE_FILE="./tmp/keys/king_harald.p12"
TRUSTSTORE_FILE="./tmp/keys/truststore.jks"
PASSWORD=password
CLIENT_JAR=debugclient/target/debugclient-1.0-SNAPSHOT-jar-with-dependencies.jar
SERVER_URI="amqps://${SERVER}:5671"
SEND_QUEUE=onramp
RECEIVE_QUEUE=king_harald
USER=king_harald

java \
    -Djavax.net.ssl.keyStore=${KEYSTORE_FILE} \
    -Djavax.net.ssl.keyStorePassword=${PASSWORD} \
    -Djavax.net.ssl.keyStoreType=pkcs12 \
    -Djavax.net.ssl.trustStore=${TRUSTSTORE_FILE} \
    -Djavax.net.ssl.trustStorePassword=${PASSWORD} \
    -DUSER=${USER} \
    -jar ${CLIENT_JAR} \
    ${SERVER_URI} ${SEND_QUEUE} ${RECEIVE_QUEUE}
