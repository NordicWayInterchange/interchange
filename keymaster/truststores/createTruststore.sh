#!/usr/bin/env bash

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <cert-file> <keystore-file-name> <keystore-password>"
fi

CERT_FILE=$1
OUT_FILENAME=$2
PASSWORD=$3
keytool -import -trustcacerts -file $CERT_FILE -keystore $OUT_FILENAME -storepass $PASSWORD -noprompt
echo "Truststore $OUT_FILENAME generated"