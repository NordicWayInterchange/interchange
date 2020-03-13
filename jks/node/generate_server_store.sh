#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "USAGE: $0 <USER_NAME> <CA_NAME> <STORE_PASSWORD>"
    exit 1
fi

NAME=$1
CA=$2
PASSWORD=$3
openssl pkcs12 \
	-export \
	-out chain.${NAME}.p12 \
	-inkey ca/intermediate/private/${NAME}.key.pem \
	-in ca/intermediate/certs/chain.${NAME}.crt.pem \
	-name ${NAME} -CAfile ca/intermediate/certs/chain.${CA}.crt.pem \
	-chain \
	-password pass:${PASSWORD}
