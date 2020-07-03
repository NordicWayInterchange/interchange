#!/bin/bash

#Fully Qualified Domain Name

if [ ! -d "ca/intermediate" ]; then
	echo No intermediate CAs created. exiting
	exit 1
fi

echo Enter the file name for the CSR:
read CSR_FILE

if [ ! -f "$CSR_FILE" ]; then
	echo The CSR file did not exist
	exit 1
fi

extractCommonName() {
  local subjects="${1//Subject:/}" # remove the prefix 'Subject=' from the subject line
  IFS=',' read -ra SUBJS <<< "$subjects" # split each subject to elements in an array
  for subject in "${SUBJS[@]}"; do
    local subject_key=$(echo $subject | cut -d '=' -f1)
    local subject_key_trim=${subject_key// /}

    if [[ ${subject_key_trim} == 'CN' ]]; then
      echo $(echo $subject | cut -d '=' -f2)
    fi
  done
}

CSR_SUBJECT_LINE=$(openssl req -noout -subject -in $CSR_FILE)
ident="$(extractCommonName "$CSR_SUBJECT_LINE")"

if [ -z "$ident" ]; then
  echo "Could not extract the common name (CN) from the subject line of the CSR"
  echo "Subject line: $CSR_SUBJECT_LINE"
  exit 1
fi

echo Ready to sign the certificate request from \"$ident\"

sed "s/FQDN/$FQDN/g" serverCert.tmpl > openssl_csr_san.cnf

echo Enter DOMAINNAME for the intermediate CA you want to use:
read CADOMAINNAME

if [ ! -f "ca/intermediate/certs/int.$CADOMAINNAME.crt.pem" ]; then
  echo could not find cert for $CADOMAINNAME. Exiting.
  exit 1
fi

cp $CSR_FILE ca/intermediate/csr/$ident.csr.pem

sed "s/DOMAIN/$CADOMAINNAME/g" inter.tmpl > openssl_intermediate.cnf 

openssl ca -config openssl_intermediate.cnf -extensions usr_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$ident.csr.pem -out ca/intermediate/certs/$ident.crt.pem

cat ca/intermediate/certs/$ident.crt.pem ca/intermediate/certs/chain.$CADOMAINNAME.crt.pem > ca/intermediate/certs/chain.$ident.crt.pem

echo "Send the file ca/intermediate/certs/chain.$ident.crt.pem to the client $ident"
