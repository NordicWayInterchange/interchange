#!/bin/bash

#Fully Qualified Domain Name
if [ "$#" -ne 3 ]; then
    echo Illegal number of arguments.
    echo "USAGE: $0 <client-identifier> <client country identifier, eg NO, upper case> <CA_DOMAIN_NAME>"
    exit 1
fi

if [ ! -d "ca/intermediate" ]; then
	echo no intermediate CAs created. exiting
	exit 1
fi

ident=$1
CADOMAINNAME=$2

if [ ! -f "ca/intermediate/certs/int.$CADOMAINNAME.crt.pem" ]; then 
        echo could not find cert for $CADOMAINNAME. Exiting.
        exit 1
fi

sed "s/DOMAIN/$CADOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf 

openssl req -out ca/intermediate/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout ca/intermediate/private/$ident.key.pem -subj "/CN=$ident/O=Nordic Way/C=NO"
openssl ca -config openssl_intermediate.cnf -extensions usr_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$ident.csr.pem -out ca/intermediate/certs/$ident.crt.pem

cat ca/intermediate/certs/$ident.crt.pem ca/intermediate/certs/chain.$CADOMAINNAME.crt.pem > ca/intermediate/certs/chain.$ident.crt.pem


