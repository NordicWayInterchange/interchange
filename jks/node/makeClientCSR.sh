#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "USAGE $0 <user ident>"
  exit 1
fi

if [ ! -d "client/csr" ]; then
  mkdir -p client/csr
fi

if [ ! -d "client/private" ]; then
  mkdir -p client/private
fi

#Fully Qualified Domain Name

echo Enter an identifier for the client:
#read ident
ident=$1

openssl req -out client/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout client/private/$ident.key.pem -config openssl_csr_san.cnf -subj "/CN=${ident}/O=Nordic Way/C=NO"

