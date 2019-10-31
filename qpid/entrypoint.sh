#!/bin/bash -eu

pushd ${QPID_WORK}

verifyExternalName() {
    if [[ ! -f ${SERVER_CERTIFICATE_FILE} ]]; then
        echo "file does not exist ${SERVER_CERTIFICATE_FILE}"
        return 2
    fi
    echo "verifying that vhost name and certificate CN match..."
    local vhost_name="$(jq -r '.name' < ${VHOST_FILE})"
    local cert_cn="$(openssl x509 -noout -subject -in ${SERVER_CERTIFICATE_FILE} | sed 's/\//\n/g' | grep '^CN=' | cut -d= -f2)"
    if [[ ${vhost_name} != ${cert_cn} ]]; then
        echo "ERROR - vhost '${vhost_name}' does not match CN '${cert_cn}'"
        return 1
    fi
}

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

verifyExternalName

echo "${VHOST_FILE} ${SERVER_PRIVATE_KEY_FILE} ${SERVER_CERTIFICATE_FILE} ${CA_CERTIFICATE_FILE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

for file in $(find ${QPID_WORK} -name *.lck); do
    set -x
    rm -f "$file"
    set +x
done

/usr/local/qpid/bin/qpid-server

