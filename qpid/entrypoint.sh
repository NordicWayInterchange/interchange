#!/bin/bash -eu

pushd ${QPID_WORK}

ENV=${1:-default}

verifyExternalName() {
    echo "verifying that vhost name and certificate CN match..."
    local vhost_name="$(jq -r '.name' < ${VHOST_FILE})"
    local cert_cn="$(openssl x509 -noout -subject -in ${SERVER_CERTIFICATE_FILE} | sed 's/\//\n/g' | grep '^CN=' | cut -d= -f2)"
    if [[ ${vhost_name} != ${cert_cn} ]]; then
        echo "ERROR - vhost '${vhost_name}' does not match CN '${cert_cn}'"
        return 1
    fi
}

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

if [[ ${ENV} == "dev" ]]; then
    echo "${CA_CN} ${SERVER_CN}" > /dev/null
    export CA_CERTIFICATE_FILE=/tmp/keys/${CA_CN}.crt
    export CA_PRIVATE_KEY_FILE=/tmp/keys/${CA_CN}.key
    export SERVER_CERTIFICATE_FILE=/tmp/keys/${SERVER_CN}.crt
    export SERVER_PRIVATE_KEY_FILE=/tmp/keys/${SERVER_CN}.key
    /scripts/generate-keys.sh /tmp
else
    verifyExternalName
fi

echo "${VHOST_FILE} ${SERVER_PRIVATE_KEY_FILE} ${SERVER_CERTIFICATE_FILE} ${CA_CERTIFICATE_FILE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

for file in $(find ${QPID_WORK} -name *.lck); do
    set -x
    rm -f "$file"
    set +x
done

/usr/local/qpid/bin/qpid-server

