#!/bin/bash -eu

extractCommonName() {
  local subjects="$(echo $1 | cut -d ':' -f2)"
  IFS=',' read -ra ADDR <<< "$subjects"
  for subject in "${ADDR[@]}"; do
    local subject_key=$(echo $subject | cut -d '=' -f1)
    local subject_key_trim=${subject_key// /}

    if [[ ${subject_key_trim} == 'CN' ]]; then
      echo $(echo $subject | cut -d '=' -f2)
    fi
  done
}


svinz="$(extractCommonName 'Subject: CN = svinz, O = Nordic Way, C = NO')"
sintef="$(extractCommonName 'Subject: C = no, ST = oslo, L = oslo, O = sintef, CN = sintef')"

echo "svinz = '${svinz}'"
echo "sintef = '${sintef}'"
