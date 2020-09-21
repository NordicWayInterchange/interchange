#!/bin/bash -eu

ENV=${1:-default}

echo "${CA_CN} ${KEY_CNS}" > /dev/null
/scripts/generate-keys.sh /jks/keys


