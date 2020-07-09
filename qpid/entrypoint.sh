#!/bin/bash -eu

pushd ${QPID_WORK}

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
    if [[ ! -f ${SERVER_CERTIFICATE_FILE} ]]; then
        echo "file does not exist ${SERVER_CERTIFICATE_FILE}"
        return 2
    fi
    echo "verifying that vhost name and certificate CN match..."
    local vhost_name="$(jq -r '.name' < ${VHOST_FILE})"
    local subject_line="$(openssl x509 -noout -subject -in ${SERVER_CERTIFICATE_FILE})"
    echo "Subjectline: '${subject_line}'"
    local cert_cn=$(extractCommonName "$subject_line")

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

