#!/bin/bash -eu

pushd ${QPID_WORK}

ENV=${1:-default}

echo "${VHOST_FILE} ${PRIVATE_KEY_FILE} ${CERTIFICATE_FILE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

for file in $(find ${QPID_WORK} -name *.lck); do
    set -x
    rm -f "$file"
    set +x
done

/usr/local/qpid/bin/qpid-server

