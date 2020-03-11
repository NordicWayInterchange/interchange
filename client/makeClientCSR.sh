#!/bin/bash

if [ ! -d "pki" ]; then
  mkdir pki
fi

echo Enter an identifier for the client:
read ident

openssl req -out pki/$ident.csr.pem -newkey rsa:2048 -nodes -keyout pki/$ident.key.pem