#!/bin/bash

if [ "$1" == "" ]; then
	echo Usage: $0 path/to/csrFile.csr
	exit 1
fi

csrPath=$1

if [ ! -d "ca" ]; then
	echo no ca dir in this folder
	exit 1
fi

if [ ! -d "ca/intermediate" ]; then
	mkdir ca/intermediate
	cd ca/intermediate
	mkdir certs #newcerts crl csr private
	touch index.txt
	touch index.txt.attr
	cd ../..
fi

echo Enter domain name for the intermediateCA:
read DOMAINNAME

#sed "s/DOMAIN/$DOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf
#openssl req -config openssl_intermediate.cnf -new -newkey rsa:4096 -keyout ca/intermediate/private/int.$DOMAINNAME.key.pem -out ca/intermediate/csr/int.$DOMAINNAME.csr
#openssl req -nodes -config openssl_intermediate.cnf -new -newkey rsa:4096 -keyout ca/intermediate/private/int.$DOMAINNAME.key.pem -out ca/intermediate/csr/int.$DOMAINNAME.csr

echo signing..

openssl ca -config openssl_root.cnf -extensions v3_intermediate_ca -days 3650 -notext -md sha512 -in $csrPath -out ca/intermediate/certs/int.$DOMAINNAME.crt.pem

CAcert=$(find ca/certs/ -name "ca*")

cat ca/intermediate/certs/int.$DOMAINNAME.crt.pem $CAcert > ca/intermediate/certs/chain.$DOMAINNAME.crt.pem



