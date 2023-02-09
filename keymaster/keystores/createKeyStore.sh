#!/usr/bin/env bash

if [ "$#" -ne 6 ]; then
  echo "Usage: $0 <key-file> <cert-file> <entry-name> <ca-cert-file> <keystore password> <output keystore>"
fi

IN_KEY=$1
IN_CERT=$2
ENTRY_NAME=$3
CA_CERT=$4
PASSWORD=$5
OUT_KEYSTORE=$6
openssl pkcs12 -export -out $OUT_KEYSTORE -inkey $IN_KEY -in $IN_CERT -name $ENTRY_NAME -CAfile $CA_CERT -password pass:$PASSWORD
echo "Keystore $OUT_KEYSTORE generated"