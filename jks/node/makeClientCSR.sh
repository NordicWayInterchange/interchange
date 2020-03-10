#!/bin/bash

if [ ! -d "client/csr" ]; then
  mkdir client/csr
fi

if [ ! -d "client/private" ]; then
  mkdir client/private
fi

#Fully Qualified Domain Name

echo Enter an identifier for the client:
read ident

openssl req -out client/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout client/private/$ident.key.pem -config openssl_csr_san.cnf

