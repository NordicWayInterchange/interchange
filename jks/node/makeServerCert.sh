#!/bin/bash

#Fully Qualified Domain Name

if [ "$#" -ne 2 ]; then
    echo "USAGE: $0 <server FQDN> <ca-domainname>"
    exit 1
fi

if [ ! -d "ca/intermediate" ]; then
	echo no intermediate CAs created. exiting
	exit 1
fi

echo Enter fully qualified domain name FQDN for the server:
#read FQDN
FQDN=$1

sed "s/FQDN/$FQDN/g" serverCert.tmpl > openssl_csr_san.cnf
#CADOMAINNAME=$2

echo Enter DOMAINNAME for the intermediate CA:
#read CADOMAINNAME
CADOMAINNAME=$2

if [ ! -f "ca/intermediate/certs/int.$CADOMAINNAME.crt.pem" ]; then 
	echo could not find cert for $CADOMAINNAME. Exiting.
	exit 1
fi

sed "s/DOMAIN/$CADOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf

openssl req -out ca/intermediate/csr/$FQDN.csr.pem -newkey rsa:2048 -nodes -keyout ca/intermediate/private/$FQDN.key.pem -config openssl_csr_san.cnf -subj "/CN=${FQDN}/O=Nordic Way/C=NO"

openssl ca -config openssl_intermediate.cnf -extensions server_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$FQDN.csr.pem -out ca/intermediate/certs/$FQDN.crt.pem

cat ca/intermediate/certs/$FQDN.crt.pem ca/intermediate/certs/chain.$CADOMAINNAME.crt.pem > ca/intermediate/certs/chain.$FQDN.crt.pem
