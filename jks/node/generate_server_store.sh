#!/bin/bash
#root-ca argument ca.interchange.kyrre.priv.no
#makeCSR ca.nodea.interchange.kyrre.priv.no
#signCRS ca.nodea.interchange.kyrre.priv.no
#makseServerCert servera.interchangetestingdomain.priv.no ca.servera.interchangetestingdomain.priv.no
if [ "$#" -ne 3 ]; then
    echo "USAGE: $0 <FQDN> <ROOT_CA> <PASSWORD>"
    exit 1
fi

FQDN=$1
ROOT_CA=$2
PASSWORD=$3
openssl pkcs12 \
	-export \
	-out chain.${FQDN}.p12 \
	-inkey ca/intermediate/private/${FQDN}.key.pem \
	-in ca/intermediate/certs/chain.${FQDN}.crt.pem \
	-name ${FQDN} -CAfile ca/intermediate/certs/chain.${ROOT_CA}.crt.pem \
	-password pass:${PASSWORD} \
	-chain \
