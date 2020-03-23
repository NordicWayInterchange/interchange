#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "USAGE $0 <user ident> <country code (upper case)>"
  exit 1
fi

if [ ! -d "client/csr" ]; then
  mkdir -p client/csr
fi

if [ ! -d "client/private" ]; then
  mkdir -p client/private
fi

#Fully Qualified Domain Name
ident=$1
country=$2

openssl req -out client/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout client/private/$ident.key.pem -config openssl_csr_san.cnf -subj "/CN=${ident}/O=Nordic Way/C=$country"

