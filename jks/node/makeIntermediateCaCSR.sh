#!/bin/bash

# if [ "$1" == "" ]; then
	# echo Usage: $0 path/to/rootCAcertFile.crt.pem
	# exit 1
# fi

# rootCrtPth=$1
# rootCrtFileName="$(basename -- $rootCrtPth)"

if [ "$#" -ne 1 ]; then
    echo "USAGE: $0 <intermediateCA domain name> <country code (upper case)>"
    exit 1
fi

if [ ! -d "ca" ]; then
        mkdir ca
        cd ca
        mkdir certs #newcerts crl csr private
        cd ..
fi

# cp $rootCrtPth ca/certs/$rootCrtFileName

if [ ! -d "ca/intermediate" ]; then
        mkdir ca/intermediate
        cd ca/intermediate
        mkdir certs newcerts crl csr private
		touch index.txt
		touch index.txt.attr
		echo 1000 > crlnumber
		echo '1234' > serial
        cd ../..
fi

echo Enter domain name for the intermediateCA:
#read DOMAINNAME
DOMAINNAME=$1
COUNTRY=$2

sed "s/DOMAIN/$DOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf

#create CSR:
openssl req -nodes -config openssl_intermediate.cnf -new -newkey rsa:4096 -keyout ca/intermediate/private/int.$DOMAINNAME.key.pem -out ca/intermediate/csr/int.$DOMAINNAME.csr -subj "/CN=${DOMAINNAME}/O=Nordic Way/C=${COUNTRY}"

echo Certificate Signing Request file created: ca/intermediate/csr/int.$DOMAINNAME.csr