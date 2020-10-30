#!/bin/bash -eu

set -o pipefail

extractCommonName() {
  local subjects="${1//subject=/}" # remove the prefix 'subject=' from the subject line

  local separator="," # check which separator is used
  if [[ "$subjects" == *\/* ]]
  then
    separator="\/"
  fi

  IFS="$separator" read -ra SUBJS <<< "$subjects" # split each subject to elements in an array
  for subject in "${SUBJS[@]}"; do
    local subject_key=$(echo $subject | cut -d '=' -f1)
    local subject_key_trim=${subject_key// /}

    if [[ ${subject_key_trim} == 'CN' ]]
    then
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
    echo "Subjectline: '${subject_line}'"
    local cert_cn=$(extractCommonName "$subject_line")

    if [[ ${VHOST_NAME} != ${cert_cn} ]]; then
        echo "ERROR - vhost '${VHOST_NAME}' does not match CN '${cert_cn}'"
        return 1
    fi
}

verifyExternalName

${QPID_HOME}/bin/qpid-server

