# Create keystore and truststore for Nordic Way Interchange user

We received a user name, password and a certificate bundle from Nordic Way.

The certificate bundle contained the 
* user private key
* public ca-certificate 
* public user certificate
formatted like this:

```
Bag Attributes
    friendlyName: nw_bouvet
    localKeyID: A9 5D BC FF 4D E4 8C E5 1A 43 6F 59 BE 00 3B FF F5 80 91 26 
Key Attributes: <No Attributes>
-----BEGIN PRIVATE KEY-----
MIIE-bla-di-blah user-private-key
d
-----END PRIVATE KEY-----
Bag Attributes
    friendlyName: CA
subject=/CN=NordicWayCA
issuer=/CN=NordicWayCA
-----BEGIN CERTIFICATE-----
MIIC-bla-di-blah CA-certificate
LS
-----END CERTIFICATE-----
Bag Attributes
    friendlyName: nw_bouvet
    localKeyID: A9 5D BC FF 4D E4 8C E5 1A 43 6F 59 BE 00 3B FF F5 80 91 26 
subject=/CN=nw_bouvet
issuer=/CN=NordicWayCA
-----BEGIN CERTIFICATE-----
MIIC-bla-di-blah user-certificate
==
-----END CERTIFICATE-----
```

The private key, certificates was extracted from the certificate bundle file into separate files like this:

| File | Cert/key |
| ------------- |-------------| 
| nw_bouvet_private.key | only the private key for the user nw_bouvet|
| nw_bouvet.cert | the signed certificate for the user nw_bouvet |
| nw.cert| NordicWayCA certificate|

### Verify key chain
This step is optional, but for debugging purposes you can verify that the user certificate is signed with the supplied CA-certificate [2].  
```
openssl verify -CAfile nw.cert nw_bouvet.cert
```
Results in: *nw_bouvet.cert: OK*

### Private key to keystore
In order to import the private key into a keystore we have to use a p12 keystore [1].   

The p12-file can be generated with openssl (from cygwin with winpty).
Run without "winpty" on a ordinary unix environment.  

Here we give an alias to the user certificate equals to the user name "nw_bouvet".

```
winpty openssl pkcs12 -export -out nw_bouvet.p12 -inkey nw_bouvet_private.key -in nw.cert -in nw_bouvet.cert -CAfile nw.cert -chain -name nw_bouvet
```
Supply a password to protect the p12-file.

Verify that the keystore contains a PrivateKeyEntry and has the key chain to the issuer NordicWayCA.
```
keytool -list -v -keystore keystore.jks -storepass <the password>
```
Look for these properties in the listing:
```
Alias name: nw_bouvet
Entry type: PrivateKeyEntry
Certificate chain length: 2
```

### Trust store
TODO: Turn off server verification

The staging server does not list the NordicWayCA certificate as a possible certificate. 
We add the staging server certificate to a new trust store.  [3] 
```
keytool -import -trustcacerts -alias interchange-staging -file interchange-staging.cert -keystore truststore.jks -storepass <the password>
```

#Sources
1. https://serverfault.com/questions/483465/import-of-pem-certificate-chain-and-key-to-java-keystore
2. https://kb.op5.com/pages/viewpage.action?pageId=19073746#sthash.QBqb2nEn.dpbs
3. https://www.feistyduck.com/library/openssl-cookbook/online/ch-testing-with-openssl.html