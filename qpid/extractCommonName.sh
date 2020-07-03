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

svinz="$(extractCommonName 'subject=CN = svinz, O = Nordic Way, C = NO')"
sintef="$(extractCommonName 'subject=C = no, ST = oslo, L = oslo, O = sintef, CN = sintef')"

echo "svinz = '${svinz}'"
echo "sintef = '${sintef}'"
