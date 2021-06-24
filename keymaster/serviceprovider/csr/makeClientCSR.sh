#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "USAGE $0 <user ident> <country code (upper case)>"
  exit 1
fi

INTERNAL_KEYS_FOLDER="/int_keys"

if [ ! -d "$INTERNAL_KEYS_FOLDER" ]; then
  echo "Output folder does not exist. Is it mapped properly?"
  exit 1
fi

if [ ! -d "client/csr" ]; then
  mkdir -p client/csr
fi

if [ ! -d "client/private" ]; then
  mkdir -p client/private
fi


cat << EOF > openssl_csr_san.cnf
[ req ]
default_bits       = 2048
distinguished_name = req_distinguished_name
req_extensions     = req_ext

[ req_distinguished_name ]
countryName                = Country Name (2 letter code)
stateOrProvinceName        = State or Province Name (full name)
localityName               = Locality Name (eg, city)
organizationName           = Organization Name (eg, company)
commonName                 = Common Name (e.g. server  or YOUR name)


# Optionally, specify some defaults.
#countryName_default             = [2 letter country code]
#stateOrProvinceName_default     = [State or Province]
#localityName_default            = [City or Town]
#0.organizationName_default      = [Organization]
#organizationalUnitName_default  = [Fully Qualified Domain Name]
#emailAddress_default            = [your email address]

[ req_ext ]
subjectAltName = @alt_names

[alt_names]
DNS.1	=
#DNS.2	= [Any variation of F.Q.D.N]
#DNS.3	= [Any variation of F.Q.D.N]
EOF

#Fully Qualified Domain Name
ident=$1
country=$2

openssl req -out client/csr/$ident.csr.pem -newkey rsa:2048 -nodes -keyout client/private/$ident.key.pem -config openssl_csr_san.cnf -subj "/emailAddress=test@test.no/CN=${ident}/O=Nordic Way/C=$country"
cp client/private/$ident.key.pem $INTERNAL_KEYS_FOLDER/
cp client/csr/$ident.csr.pem $INTERNAL_KEYS_FOLDER/
chmod ugo+rwx $INTERNAL_KEYS_FOLDER/*
echo Key and CSR created