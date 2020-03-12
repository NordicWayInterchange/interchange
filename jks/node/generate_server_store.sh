#!/bin/bash -x
#root-ca argument ca.interchange.kyrre.priv.no
#makeCSR ca.nodea.interchange.kyrre.priv.no
#signCRS ca.nodea.interchange.kyrre.priv.no
#makseServerCert servera.interchangetestingdomain.priv.no ca.servera.interchangetestingdomain.priv.no
FQDN=$1
ROOT_CA=$2
PASSWORD=$3
openssl pkcs12 \
	-export \
	-out chain.${FQDN}.p12 \
	-inkey ca/intermediate/private/${FQDN}.key.pem \
	-in ca/intermediate/certs/chain.${FQDN}.crt.pem \
	-name ${FQDN} -CAfile ca/certs/ca.${ROOT_CA} \
	-password pass:${PASSWORD}
