#!/bin/bash -eu

pushd ${QPID_WORK}

mkdir -p ./default/config

# Copy groups and vhost file from static config directory if they do not exist in the work/default/config directory
if [[ ! -f ${GROUPS_FILE} ]]; then
    echo "groups file not in ${GROUPS_FILE}, copying from static file ${STATIC_GROUPS_FILE}"
    cp ${STATIC_GROUPS_FILE} ${GROUPS_FILE}
else
    echo "groups file already exists in ${GROUPS_FILE}"
fi

if [[ ! -f ${VHOST_FILE} ]]; then
    echo "vhost file does not exist in ${VHOST_FILE}, copying from static file ${STATIC_VHOST_FILE}"
    cp ${STATIC_VHOST_FILE} ${VHOST_FILE}
else
    echo "vhost file already exists in ${VHOST_FILE}"
fi


echo "${VHOST_FILE} ${KEY_STORE} ${KEY_STORE_PASSWORD} ${TRUST_STORE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

for file in $(find ${QPID_WORK} -name *.lck); do
    set -x
    rm -f "$file"
    set +x
done

/usr/local/qpid/bin/qpid-server

