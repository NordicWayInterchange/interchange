#!/bin/bash

if [ "$#" -ne 5 ]; then
    echo "USAGE: $0 <server FQDN> <ca-key> <ca-cert> <ca-cart-chain> <country-code>"
    exit 1
fi

FQDN=$1
CA_KEY=$2
CA_CERT=$3
CA_CHAIN=$4
COUNTRY_CODE=$5

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
commonName                 = Common Name (e.g. server FQDN or YOUR name)

# Optionally, specify some defaults.
#countryName_default             = [2 letter country code]
#stateOrProvinceName_default     = [State or Province]
#0.organizationName_default      = [Organization]
#organizationalUnitName_default  = [Fully Qualified Domain Name]
#emailAddress_default            = [your email address]

[ req_ext ]
subjectAltName = @alt_names

[alt_names]
DNS.1	= $FQDN
#DNS.2	= [Any variation of F.Q.D.N]
#DNS.3	= [Any variation of F.Q.D.N]
EOF



if [ ! -f "$CA_KEY" ]; then
	echo could not find key for intermediate CA. Exiting.
	exit 1
fi

if [ ! -f "$CA_CERT" ]; then
	echo could not find cert for intermediate CA. Exiting.
	exit 1
fi

if [ ! -f "$CA_CHAIN" ]; then
	echo could not find cert chain for intermediate CA. Exiting.
	exit 1
fi

if [ ! -d ca ]; then
  mkdir -p ca/intermediate/{private,newcerts,certs,csr}
  touch ca/intermediate/index.txt
  echo 1000 > ca/intermediate/serial
fi

cat << EOF > openssl_intermediate.cnf
# OpenSSL intermediate CA configuration file.
# Copy to '/root/ca/intermediate/openssl.cnf'.

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
private_key       = $CA_KEY
certificate       = $CA_CERT

# For certificate revocation lists.
crlnumber         = ca/intermediate/crlnumber
crl               = ca/intermediate/crl/int.$FQDN.crl.pem
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

openssl req -out ca/intermediate/csr/$FQDN.csr.pem -newkey rsa:2048 -nodes -keyout ca/intermediate/private/$FQDN.key.pem -config openssl_csr_san.cnf -subj "/CN=${FQDN}/O=Nordic Way/C=${COUNTRY_CODE}"
openssl ca -batch -config openssl_intermediate.cnf -extensions server_cert -days 3750 -notext -md sha512 -in ca/intermediate/csr/$FQDN.csr.pem -out ca/intermediate/certs/$FQDN.crt.pem
cat ca/intermediate/certs/$FQDN.crt.pem $CA_CHAIN > ca/intermediate/certs/chain.$FQDN.crt.pem
chmod -R ugo+rwx ca/
cp ca/intermediate/private/$FQDN.key.pem /keys_out/
cp ca/intermediate/certs/$FQDN.crt.pem /keys_out/
cp ca/intermediate/certs/chain.$FQDN.crt.pem /keys_out/