#!/bin/bash -eu

# uses password "password" on keystores and truststore

pushd $(dirname $0)
SERVER_CN=${SERVER_CN:-localhost}
USER_CNS=${USER_CNS:-king_gustaf king_harald}
KEYS_DIR=tmp/keys

serverCert() {
    echo "generating server side certificate with common name '${SERVER_CN}'"
    openssl req -new -newkey rsa:2048 -nodes -keyout server.key \
        -out server.csr -subj "/CN=${SERVER_CN}/O=Nordic Way/C=NO"
    openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
    rm -f server.csr server.srl
    keytool -import -trustcacerts -file server.crt -keystore truststore.jks \
        -storepass password -noprompt
	openssl pkcs12 -export -out server.p12 -inkey server.key -in server.crt \
	    -chain -CAfile server.crt -password pass:password -name server
}

userCert() {
    local cn=$1
    echo "generating user side certificate with common name '${cn}'"
    openssl req -new -newkey rsa:2048 -nodes -keyout ${cn}.key -out ${cn}.csr \
        -subj "/CN=${cn}/O=Nordic Way/C=NO"
    openssl x509 -req -days 365 -in ${cn}.csr -out ${cn}.crt -CA server.crt \
        -CAkey server.key -CAcreateserial -sha256
    rm -f ${cn}.csr
    openssl pkcs12 -export -out ${cn}.p12 -inkey ${cn}.key -in ${cn}.crt \
        -CAfile server.crt -password pass:password
}

rm -rf ${KEYS_DIR} && mkdir -p ${KEYS_DIR} && pushd ${KEYS_DIR}
serverCert
for user in ${USER_CNS}; do
    userCert ${user}
done
