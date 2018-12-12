#!/bin/bash -eu

set -x

pushd ${QPID_WORK}

ENV=${1:-default}
VHOST_NAME="$(jq -r '.name' < ${VHOST_FILE})"

verifyExternalName() {
    echo "verifying that vhost name and certificate CN match..."
    local vhost_name="$(jq -r '.name' < ${VHOST_FILE})"
    local cert_cn="$(openssl x509 -noout -subject -in ${CERTIFICATE_FILE} | sed 's/\//\n/g' | grep '^CN=' | cut -d= -f2)"
    if [[ ${vhost_name} != ${cert_cn} ]]; then
        echo "ERROR - vhost ${vhost_name} does not match CN ${cert_cn}"
        return 1
    fi
}

echo "${VHOST_FILE} ${PRIVATE_KEY_FILE} ${CERTIFICATE_FILE} ${PASSWD_FILE} ${GROUPS_FILE}" > /dev/null

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

if [[ ${ENV} == "dev" ]]; then
    /scripts/generate-keys.sh
else
    verifyExternalName
fi

for file in $(find ${QPID_WORK} -name *.lck); do
    rm -f "$file"
done



/usr/local/qpid/bin/qpid-server
