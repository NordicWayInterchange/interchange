#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo no domainname for the root ca
    echo "USAGE: $0 <ca-domainname> <country code (upper case)>"
    exit 1
fi
KEYS_OUT_DIR="/ca_keys"
if [ ! -d "$KEYS_OUT_DIR" ]; then
	echo "Could not find ca-keys directory to write keys to, exiting"
	exit 1
fi

mkdir -p ca/{newcerts,certs,crl,private,requests}
touch ca/index.txt
touch ca/index.txt.attr
echo '1000' > ca/serial

#echo Enter domain name for the rootCA:
DOMAINNAME=$1
COUNTRY_CODE=$2
KEY_FILE=ca.$DOMAINNAME.key.pem
CERT_FILE=ca.$DOMAINNAME.crt.pem

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
private_key       = ca/private/$KEY_FILE
certificate       = ca/certs/$CERT_FILE

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

openssl genrsa -out ca/private/$KEY_FILE 4096
openssl req -config openssl_root.cnf -new -x509 -extensions v3_ca -key ca/private/$KEY_FILE -out ca/certs/$CERT_FILE -days 3650 -set_serial 0 -subj "/CN=${DOMAINNAME}/O=Nordic Way/C=${COUNTRY_CODE}"
cp ca/private/$KEY_FILE "$KEYS_OUT_DIR"
cp ca/certs/$CERT_FILE "$KEYS_OUT_DIR"
chmod ugo+rwx "$KEYS_OUT_DIR"*
echo "CA Key generation done"

