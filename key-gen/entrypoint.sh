#!/bin/bash -eu

ENV=${1:-default}

echo "${CA_CN} ${KEY_CNS}" > /dev/null
export CA_CERTIFICATE_FILE=/jks/keys/${CA_CN}.crt
export CA_PRIVATE_KEY_FILE=/jks/keys/${CA_CN}.key
/scripts/generate-keys.sh /tmp


