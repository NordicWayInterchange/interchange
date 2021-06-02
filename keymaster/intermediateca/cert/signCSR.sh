#!/bin/bash

if [ "$#" -ne 4 ]; then
	echo "Usage: $0 <path/to/csrFile.csr> <intermediateCA domain name> <path/to/ca_cert> <path_to_ca_key>"
	exit 1
fi

csrPath=$1
if [ ! -f "$1" ]; then
  echo "Input file $1 not found"
	exit 1
fi

DOMAINNAME=$2
CA_CERT=$3
CA_KEY=$4

if [ ! -d "ca/intermediate" ]; then
	mkdir -p ca/intermediate/certs
	touch ca/intermediate/index.txt
	touch ca/intermediate/index.txt.attr
fi


cat << EOF > openssl_root.cnf
# OpenSSL root CA configuration file.
[ ca ]
# man ca
default_ca = CA_default

[ CA_default ]
# Directory and file locations.
dir               = ca
certs             = ca/certs
crl_dir           = ca/crl
new_certs_dir     = ca/newcerts
database          = ca/index.txt
serial            = ca/serial
RANDFILE          = ca/private/.rand

# The root key and root certificate.
private_key       = $CA_KEY
certificate       = $CA_CERT

# For certificate revocation lists.
crlnumber         = ca/crlnumber
crl               = ca/crl/ca.$DOMAINNAME.crl.pem
crl_extensions    = crl_ext
default_crl_days  = 30

# SHA-1 is deprecated, so use SHA-2 instead.
default_md        = sha512
name_opt          = ca_default
cert_opt          = ca_default
default_days      = 375
preserve          = no
policy            = policy_strict

[ policy_strict ]
# The root CA should only sign intermediate certificates that match.
# See the POLICY FORMAT section of 'man ca'.
#changed from match
countryName             = optional
#changed from match
stateOrProvinceName     = optional
#changed from match
organizationName        = supplied
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
#countryName_default             = [2 letter contry code]
#stateOrProvinceName_default     = [State or Province]
#localityName_default            = [City or Town]
#0.organizationName_default      = [Name of the organization]
#organizationalUnitName_default  = [Unit]
#emailAddress_default            = [your email address]

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
nsCertType = server
nsComment = "OpenSSL Generated Server Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth

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

openssl ca -config openssl_root.cnf -extensions v3_intermediate_ca -days 3650 -notext -md sha512 -in $csrPath -out ca/intermediate/certs/int.$DOMAINNAME.crt.pem
#TODO we need to think about this, will it work for multiple nodes?
CAcert=$(find ca/certs/ -name "ca*")

cat ca/intermediate/certs/int.$DOMAINNAME.crt.pem $CAcert > ca/intermediate/certs/chain.$DOMAINNAME.crt.pem
echo Cert signing complete.



