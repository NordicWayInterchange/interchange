#!/bin/bash

if [ -d "ca" ]; then
	echo ca dir already present, exiting.
	exit 1
fi

mkdir ca
cd ca
mkdir newcerts certs crl private requests
touch index.txt
touch index.txt.attr
echo '1000' > serial
cd ..

echo Enter domain name for the rootCA:
read DOMAINNAME

sed "s/DOMAINNAME/$DOMAINNAME/g" root.tmpl > openssl_root.cnf

#openssl genrsa -aes256 -out ca/private/ca.$DOMAINNAME.key.pem 4096
openssl genrsa -out ca/private/ca.$DOMAINNAME.key.pem 4096

#openssl req -config openssl_root.cnf -new -x509 -sha512 -extensions v3_ca -key ca/private/ca.$DOMAINNAME.key.pem -out ca/certs/ca.$DOMAINNAME.crt.pem -days 3650 -set_serial 0
openssl req -config openssl_root.cnf -new -x509 -extensions v3_ca -key ca/private/ca.$DOMAINNAME.key.pem -out ca/certs/ca.$DOMAINNAME.crt.pem -days 3650 -set_serial 0


