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

echo "${VHOST_FILE} ${SERVER_PRIVATE_KEY_FILE} ${CA_CERTIFICATE_FILE} ${SERVER_CERTIFICATE_FILE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

verifyExternalName

for file in $(find ${QPID_WORK} -name *.lck); do
    set -x
    rm -f "$file"
    set +x
done

/usr/local/qpid/bin/qpid-server

