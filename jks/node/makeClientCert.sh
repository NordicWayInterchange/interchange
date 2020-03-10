#!/bin/bash

#Fully Qualified Domain Name
if [ "$#" -ne 2 ]; then
    echo Illegal number of arguments.
    echo "USAGE: $0 <client-identifier> <CA_DOMAIN_NAME>"
    exit 1
fi

if [ ! -d "ca/intermediate" ]; then
	echo no intermediate CAs created. exiting
	exit 1
fi

echo Enter an identifier for the client:
#read ident
ident=$1

sed "s/FQDN/$FQDN/g" serverCert.tmpl > openssl_csr_san.cnf

echo Enter DOMAINNAME for the intermediate CA you want to use:
#read CADOMAINNAME
CADOMAINNAME=$2

if [ ! -f "ca/intermediate/certs/int.$CADOMAINNAME.crt.pem" ]; then 
        echo could not find cert for $CADOMAINNAME. Exiting.
        exit 1
fi

sed "s/DOMAIN/$CADOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf 

openssl req -out ca/intermediate/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout ca/intermediate/private/$ident.key.pem -config openssl_csr_san.cnf

openssl ca -config openssl_intermediate.cnf -extensions usr_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$ident.csr.pem -out ca/intermediate/certs/$ident.crt.pem

cat ca/intermediate/certs/$ident.crt.pem ca/intermediate/certs/chain.$CADOMAINNAME.crt.pem > ca/intermediate/certs/chain.$ident.crt.pem


