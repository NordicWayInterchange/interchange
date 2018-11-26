#!/bin/bash -eu

pushd ${QPID_WORK}

echo "ENTRYPOINT - using keystore file ${KEYSTORE_FILE}"
echo "ENTRYPOINT - using truststore file ${TRUSTSTORE_FILE}"
echo "ENTRYPOINT - using vhost file ${VHOST_FILE}"

if [[ -z ${KEYSTORE_PASSWORD} || -z ${TRUSTSTORE_PASSWORD} ]]; then
    echo "ERROR - need both KEYSTORE_PASSWORD and TRUSTSTORE_PASSWORD set"
    exit 1
fi

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

/usr/local/qpid/bin/qpid-server
