clientCA = qpid
client = guest
OU=Example Org, O=Example Company => OU=interchange, O=nordicway

# Create a self signed certificate & private key to create a root certificate authority.
keytool -genkeypair -v \
  -alias clientCA \
  -keystore client.jks \
  -dname "CN=clientCA, OU=Example Org, O=Example Company, L=Oslo, ST=Oslo, C=NO" \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048 \
  -ext KeyUsage="keyCertSign" \
  -ext BasicConstraints="ca:true" \
  -validity 365

# Create another key pair that will act as the client.  We want this signed by the client CA.
keytool -genkeypair -v \
  -alias client \
  -keystore client.jks \
  -dname "CN=client, OU=Example Org, O=Example Company, L=Oslo, ST=Oslo, C=NO" \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048

# Create a certificate signing request from the client certificate.
keytool -certreq -v \
  -alias client \
  -keypass:env PW \
  -storepass:env PW \
  -keystore client.jks \
  -file client.crq

# Make clientCA create a certificate chain saying that client is signed by clientCA.
keytool -gencert -v \
  -alias clientCA \
  -keypass:env PW \
  -storepass:env PW \
  -keystore client.jks \
  -infile client.crq \
  -outfile client.crt \
  -ext EKU="clientAuth" \
  -rfc

# Export the client-ca certificate from the keystore.  This goes to nginx under "ssl_client_certificate"
# and is presented in the CertificateRequest.
keytool -export -v \
  -alias clientCA \
  -file clientca.crt \
  -storepass:env PW \
  -keystore client.jks \
  -rfc
  
# trust ca cert  
keytool -import -v \
  -trustcacerts 
  -alias clientCA 
  -file clientCA.crt 
  -keystore cacerts.jks 
  -keypass:env PW 
  -storepass:env PW

keytool -export -v \
  -alias clientCA \
  -file clientca.crt \
  -storepass:env PW \
  -keystore client.jks \
  -rfc
  

# Import the signed certificate back into client.jks.  This is important, as JSSE won't send a client
# certificate if it can't find one signed by the client-ca presented in the CertificateRequest.
keytool -import -v \
  -alias client \
  -file client.crt \
  -keystore client.jks \
  -storetype JKS \
  -storepass:env PW

# Export the client CA to pkcs12, so it's safe.
keytool -importkeystore -v \
  -srcalias clientCA \
  -srckeystore client.jks \
  -srcstorepass:env PW \
  -destkeystore clientca.p12 \
  -deststorepass:env PW \
  -deststoretype PKCS12

# Then, strip out the client CA from client.jks.
keytool -delete -v \
 -alias clientCA \
 -storepass:env PW \
 -keystore client.jks

# List out the contents of client.jks just to confirm it.
keytool -list -v \
  -keystore client.jks \
  -storepass:env PW