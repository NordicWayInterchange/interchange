#!/bin/bash -eu

# uses password "password" on keystores and truststore

pushd $(dirname $0)
CA_CN=${CA_CN:-my_ca}
KEY_CNS=${KEY_CNS:-localhost king_gustaf king_harald}
KEYS_DIR=$1
CA_CERTIFICATE_FILE=${KEYS_DIR}/${CA_CN}.crt
CA_PRIVATE_KEY_FILE=${KEYS_DIR}/${CA_CN}.key

genCaCert() {
    local cn=$1
    local key_file=${cn}.key
    local cert_file=${cn}.crt
    echo "generating CA certificate with common name '${cn}'"
    openssl req -new -newkey rsa:2048 -nodes -keyout ${key_file} \
        -out ca.csr -subj "/CN=${cn}/O=Nordic Way/C=NO"
    openssl x509 -req -days 365 -in ca.csr -signkey ${key_file} -out ${cert_file}
    rm -f ca.csr ca.srl
    keytool -import -trustcacerts -file ${cert_file} -keystore truststore.jks \
        -storepass password -noprompt
}

generateCertificate() {
    local cn=$1
    echo "generating certificate with common name '${cn}'"
    openssl req -new -newkey rsa:2048 -nodes -keyout ${cn}.key -out ${cn}.csr \
        -subj "/CN=${cn}/O=Nordic Way/C=NO"
    openssl x509 -req -days 365 -in ${cn}.csr -out ${cn}.crt -CA ${CA_CERTIFICATE_FILE} \
        -CAkey ${CA_PRIVATE_KEY_FILE} -CAcreateserial -sha256
    rm -f ${cn}.csr ${cn}.srl
    openssl pkcs12 -export -out ${cn}.p12 -inkey ${cn}.key -in ${cn}.crt -name ${cn} \
        -CAfile ${CA_CERTIFICATE_FILE} -password pass:password
}

rm -rf ${KEYS_DIR}/*
pushd ${KEYS_DIR}
chmod ugo+rw ${KEYS_DIR}

genCaCert ${CA_CN}
for commonName in ${KEY_CNS}; do
    generateCertificate ${commonName}
done
chmod ugo+rw ${KEYS_DIR}/*
echo "CERT GENERATION DONE"
