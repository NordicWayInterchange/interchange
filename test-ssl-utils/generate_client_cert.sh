#!/bin/bash
#USAGE: generate_client_cert.sh <cert_name> <ca_name>
openssl req -new -newkey rsa:2048 -nodes -keyout ${1}.key -out ${1}.csr -subj "/CN=${1}/O=Nordic Way/C=NO"
openssl x509 -req -days 365 -in ${1}.csr -out ${1}.crt -CA ${2}.crt -CAkey ${2}.key -CAcreateserial -sha256
openssl pkcs12 -export -out ${1}.p12 -inkey ${1}.key -in ${1}.crt -name ${1} -CAfile ${2}.crt -password pass:password
