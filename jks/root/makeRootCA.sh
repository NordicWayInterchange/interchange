#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo no domainname for the root ca
    echo "USAGE: $0 <ca-domainname>"
    exit 1
fi

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
DOMAINNAME=$1
CA_PEM_FILE=ca.$DOMAINNAME.key.pem
CSR_FILE=ca.$DOMAINNAME.crt.pem
sed "s/DOMAINNAME/$DOMAINNAME/g" root.tmpl > openssl_root.cnf

openssl genrsa -out ca/private/$CA_PEM_FILE 4096
openssl req -config openssl_root.cnf -new -x509 -extensions v3_ca -key ca/private/$CA_PEM_FILE -out ca/certs/$CSR_FILE -days 3650 -set_serial 0 -subj "/CN=${DOMAINNAME}/O=Nordic Way/C=NO"


