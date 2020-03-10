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

CSR_SUBJECT_LINE=$(keytool -printcertreq -file $CSR_FILE | grep Subject:)
BEFORE_FIRST_COMMA="${CSR_SUBJECT_LINE//,*/}"
ident="${BEFORE_FIRST_COMMA//*CN\=/}"

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
