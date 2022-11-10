#!/bin/bash -eu

ENV=${1:-default}

echo "${CA_CN} ${KEY_CNS}"
/scripts/generate-keys.sh /jks/keys


