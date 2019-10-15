#!/bin/bash

#Fully Qualified Domain Name

if [ ! -d "ca/intermediate" ]; then
	echo no intermediate CAs created. exiting
	exit 1
fi

echo Enter fully qualified domain name FQDN for the server:
read FQDN

sed "s/FQDN/$FQDN/g" serverCert.tmpl > openssl_csr_san.cnf

echo Enter DOMAINNAME for the intermediate CA:
read CADOMAINNAME

if [ ! -f "ca/intermediate/certs/int.$CADOMAINNAME.crt.pem" ]; then 
	echo could not find cert for $CADOMAINNAME. Exiting.
	exit 1
fi

sed "s/DOMAIN/$CADOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf

openssl req -out ca/intermediate/csr/$FQDN.csr.pem -newkey rsa:2048 -nodes -keyout ca/intermediate/private/$FQDN.key.pem -config openssl_csr_san.cnf

openssl ca -config openssl_intermediate.cnf -extensions server_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$FQDN.csr.pem -out ca/intermediate/certs/$FQDN.crt.pem

cat ca/intermediate/certs/$FQDN.crt.pem ca/intermediate/certs/chain.$CADOMAINNAME.crt.pem > ca/intermediate/certs/chain.$FQDN.crt.pem
