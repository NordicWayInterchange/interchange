#!/bin/bash -eu

extractCommonName() {
  local subjects="${1//subject=/}" # remove the prefix 'subject=' from the subject line
  IFS=',' read -ra SUBJS <<< "$subjects" # split each subject to elements in an array
  for subject in "${SUBJS[@]}"; do
    local subject_key=$(echo $subject | cut -d '=' -f1)
    local subject_key_trim=${subject_key// /}

    if [[ ${subject_key_trim} == 'CN' ]]; then
      echo $(echo $subject | cut -d '=' -f2)
    fi
  done
}

verifyExternalName() {
    if [[ ! -f ${KEY_STORE} ]]; then
        echo "file does not exist ${KEY_STORE}"
        return 2
    fi
    echo "verifying that vhost name and certificate CN match..."
    #local vhost_name="$(jq -r '.name' < ${VHOST_FILE})"
    local subject_line="$(openssl pkcs12 -nodes -passin pass:"${KEY_STORE_PASSWORD}" -nokeys -in ${KEY_STORE}| openssl x509 -noout -subject )"
    local cert_cn=$(extractCommonName "$subject_line")


    if [[ ${vhost_name} != ${cert_cn} ]]; then
        echo "ERROR - vhost '${vhost_name}' does not match CN '${cert_cn}'"
        return 1
    fi
}


KEY_STORE_PASSWORD=password

export KEY_STORE=/c/arbeid/interchange/jks/node/secrets/bouveta-fed/chain.bouveta-fed.itsinterchange.eu.p12
export vhost_name=bouveta-fed.itsinterchange.eu
echo "Verifying keystore cn"
verifyExternalName

echo "Verified keystore cn successfully"
