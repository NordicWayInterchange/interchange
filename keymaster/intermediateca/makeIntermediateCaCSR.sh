#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "USAGE: $0 <intermediateCA domain name> <country code (upper case)>"
    exit 1
fi
if [ ! -d "int_keys" ]; then
  echo "Output folder does not exist. Is it mapped properly?"
  exit 1
fi

mkdir -p ca/certs

mkdir -p ca/intermediate/{certs,newcerts,clr,csr,private}
touch ca/intermediate/index.txt
touch ca/intermediate/index.txt.attr
echo 1000 > ca/intermediate/crlnumber
echo '1234' > ca/intermediate/serial

echo Enter domain name for the intermediateCA:
DOMAINNAME=$1
COUNTRY=$2

cat << EOF > openssl_intermediate.cnf
# OpenSSL intermediate CA configuration file.

[ ca ]
# 'man ca'
default_ca = CA_default

[ CA_default ]
# Directory and file locations.
dir               = ca/intermediate
certs             = ca/intermediate/certs
crl_dir           = ca/intermediate/crl
new_certs_dir     = ca/intermediate/newcerts
database          = ca/intermediate/index.txt
serial            = ca/intermediate/serial
RANDFILE          = ca/intermediate/private/.rand

# The root key and root certificate.
private_key       = ca/intermediate/private/int.$DOMAINNAME.key.pem
certificate       = ca/intermediate/certs/int.$DOMAINNAME.crt.pem

# For certificate revocation lists.
crlnumber         = ca/intermediate/crlnumber
crl               = ca/intermediate/crl/int.$DOMAINNAME.crl.pem
crl_extensions    = crl_ext
default_crl_days  = 30

# SHA-1 is deprecated, so use SHA-2 instead.
default_md        = sha512
name_opt          = ca_default
cert_opt          = ca_default
default_days      = 3650
preserve          = no
policy            = policy_loose
#Ensure that the extensione in the CSR make it to the signed certificate (like subjectAltNames)
copy_extensions   = copy

[ policy_strict ]
# The root CA should only sign intermediate certificates that match.
# See the POLICY FORMAT section of 'man ca'.
countryName             = match
stateOrProvinceName     = match
organizationName        = match
organizationalUnitName  = optional
commonName              = supplied
emailAddress            = optional


[ policy_loose ]
# Allow the intermediate CA to sign a more diverse range of certificates.
# See the POLICY FORMAT section of the 'ca' man page.
countryName             = optional
stateOrProvinceName     = optional
localityName            = optional
organizationName        = optional
organizationalUnitName  = optional
commonName              = supplied
emailAddress            = optional


[ req ]
# Options for the 'req' tool ('man req').
default_bits        = 4096
distinguished_name  = req_distinguished_name
string_mask         = utf8only
# SHA-1 is deprecated, so use SHA-2 instead.
default_md          = sha512
# Extension to add when the -x509 option is used.
x509_extensions     = v3_ca


[ req_distinguished_name ]
countryName                     = Country Name (2 letter code)
stateOrProvinceName             = State or Province Name
localityName                    = Locality Name
0.organizationName              = Organization Name
organizationalUnitName          = Organizational Unit Name
commonName                      = Common Name
emailAddress                    = Email Address
# Optionally, specify some defaults.
#countryName_default             = [2 letter code]
#stateOrProvinceName_default     = [State or Province]
#localityName_default            = [City or Town]
#0.organizationName_default      = [Organization]
#organizationalUnitName_default  = [Unit]
#emailAddress_default            = [Your email address]

[ v3_ca ]
# Extensions for a typical CA ('man x509v3_config').
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true
keyUsage = critical, digitalSignature, cRLSign, keyCertSign

[ v3_intermediate_ca ]
# Extensions for a typical intermediate CA ('man x509v3_config').
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true, pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign


[ usr_cert ]
# Extensions for client certificates ('man x509v3_config').
basicConstraints = CA:FALSE
nsCertType = client, email
nsComment = "OpenSSL Generated Client Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
keyUsage = critical, nonRepudiation, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, emailProtection

[ server_cert ]
# Extensions for server certificates ('man x509v3_config').
basicConstraints = CA:FALSE
nsCertType = server, client
nsComment = "OpenSSL Generated Server Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
keyUsage = critical, digitalSignature, keyEncipherment, nonRepudiation
extendedKeyUsage = serverAuth, clientAuth

[ crl_ext ]
# Extension for CRLs ('man x509v3_config').
authorityKeyIdentifier=keyid:always

[ ocsp ]
# Extension for OCSP signing certificates ('man ocsp').
basicConstraints = CA:FALSE
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
keyUsage = critical, digitalSignature
extendedKeyUsage = critical, OCSPSigning
EOF

#create CSR:
openssl req -nodes -config openssl_intermediate.cnf -new -newkey rsa:4096 -keyout ca/intermediate/private/int.$DOMAINNAME.key.pem -out ca/intermediate/csr/int.$DOMAINNAME.csr -subj "/CN=${DOMAINNAME}/O=Nordic Way/C=${COUNTRY}"
cp ca/intermediate/private/int.$DOMAINNAME.key.pem /int_keys/
cp ca/intermediate/csr/int.$DOMAINNAME.csr /int_keys/
chmod ugo+rwx /int_keys/*
echo Certificate Signing Request file created: ca/intermediate/csr/int.$DOMAINNAME.csr