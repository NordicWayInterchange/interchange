#!/bin/bash -eu

# uses password "password" on keystores and truststore

SERVER_CN=${SERVER_CN:-localhost}
GUEST_CN=${GUEST_CN:-guest}

echo "generating server side certificate with common name '${SERVER_CN}'"
echo ""

rm -rf tmp/keys && mkdir -p tmp/keys && pushd tmp/keys

openssl req -new -newkey rsa:2048 -nodes -keyout server.key \
    -out server.csr -subj "/CN=${SERVER_CN}/O=Nordic Way/C=NO"

openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
rm -f server.csr

openssl pkcs12 -export -inkey server.key -in server.crt \
    -out server.p12 -password pass:password

keytool -import -trustcacerts -file server.crt -keystore truststore.jks \
    -storepass password -noprompt


echo ""
echo "generating user side certificate with common name '${GUEST_CN}'"
echo ""

openssl req -new -newkey rsa:2048 -nodes -keyout guest.key -out guest.csr \
    -subj "/CN=${GUEST_CN}/O=Nordic Way/C=NO"

openssl x509 -req -days 365 -in guest.csr -out guest.crt -CA server.crt \
    -CAkey server.key -CAcreateserial -sha256
rm -f guest.csr

openssl pkcs12 -export -out guest.p12 -inkey guest.key -in guest.crt \
    -CAfile server.crt -password pass:password
