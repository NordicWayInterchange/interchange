#!/bin/bash

#Fully Qualified Domain Name
if [ "$#" -ne 5 ]; then
    echo Illegal number of arguments.
    echo "USAGE: $0 <path/to/csrFile.csr> <client-identifier> <path/to/intermediate/ca.crt.pem> <path/to/intermediate/ca.key.pem> <path/to/intermediate/chain.crt.pem>"
    exit 1
fi
#TODO check the rest of the arguments
CSRFILE=$1
ident=$2
INTERMEDIATE_CA_CERT=$3
INTERMEDIATE_CA_KEY=$4
INTERMEDIATE_CA_CHAIN=$5
OUTPUT_PATH="/cert_out/"
CERT_OUT="ca/intermediate/certs/$ident.crt.pem"
SP_CERT_CHAIN="ca/intermediate/certs/chain.$ident.crt.pem"
#if [ ! -f "$CRSFILE" ]; then
#        echo could not find CRS for $ident. Exiting.
#        exit 1
#fi
if [ ! -f "$INTERMEDIATE_CA_CERT" ]; then
        echo could not find cert for intermediate CA. Exiting.
        exit 1
fi

if [ ! -f "$INTERMEDIATE_CA_KEY" ]; then
        echo could not find key for intermediate CA. Exiting.
        exit 1
fi
if [ ! -d "$OUTPUT_PATH" ]; then
        echo Output path does not exist. Is it mapped properly?
        exit 1
fi
if [ ! -d "ca/intermediate" ]; then
	mkdir -p ca/intermediate/{certs,newcerts,crl,private}
  touch ca/intermediate/index.txt
	touch ca/intermediate/index.txt.attr
	echo '1000'  > ca/intermediate/serial
fi

cat << EOF > openssl_intermediate.cnf
# OpenSSL intermediate CA configuration file.
[ ca ]
# man ca
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
private_key       = $INTERMEDIATE_CA_KEY
certificate       = $INTERMEDIATE_CA_CERT

# For certificate revocation lists.
crlnumber         = ca/intermediate/crlnumber
crl               = ca/intermediate/crl/crlfile
default_crl_days  = 30

# SHA-1 is deprecated, so use SHA-2 instead.
default_md        = sha512

name_opt          = ca_default
cert_opt          = ca_default
default_days      = 3650
preserve          = no

#Ensure that the extensione in the CSR make it to the signed certificate (like subjectAltNames)
copy_extensions   = copy
policy            = policy_loose

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

openssl ca -batch -config openssl_intermediate.cnf -extensions usr_cert -days 3750 -notext -md sha512 -in $CSRFILE -out $CERT_OUT
#TODO need both the cert and the chain from the CA
chmod -R ugo+rwx ca/
cat $CERT_OUT $INTERMEDIATE_CA_CHAIN > $SP_CERT_CHAIN
cp  $CERT_OUT $OUTPUT_PATH
cp  $SP_CERT_CHAIN $OUTPUT_PATH
echo Service Provider Cert signing complete

