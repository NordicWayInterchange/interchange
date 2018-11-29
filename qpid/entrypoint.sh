#!/bin/bash -eu

pushd ${QPID_WORK}

ENV=${1:-default}

echo "ENTRYPOINT - using vhost file ${VHOST_FILE}"
echo "ENTRYPOINT - using private key file ${PRIVATE_KEY_FILE}"
echo "ENTRYPOINT - using certificate file ${CERTIFICATE_FILE}"
echo "ENTRYPOINT - using users file ${PASSWD_FILE}"
echo "ENTRYPOINT - using groups file ${GROUPS_FILE}"

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

if [[ ${ENV} == "dev" ]]; then
    /scripts/generate-keys.sh
fi

/usr/local/qpid/bin/qpid-server
