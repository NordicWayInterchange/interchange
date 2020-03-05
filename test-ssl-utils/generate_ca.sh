#!/bin/bash
#USAGE: generate_ca.sh <ca-name>
openssl req -new -newkey rsa:2048 -nodes -keyout ${1}.key -out ca.csr -subj "/CN=${1}/O=Nordic Way/C=NO" 
openssl x509 -req -days 365 -in ca.csr -signkey ${1}.key -out ${1}.crt
openssl pkcs12 -export -out ${1}.p12 -inkey ${1}.key -in ${1}.crt -name ${1} -CAfile ${1}.crt -password pass:password
keytool -import -trustcacerts -file ${1}.crt -keystore truststore.jks -storepass password -noprompt
