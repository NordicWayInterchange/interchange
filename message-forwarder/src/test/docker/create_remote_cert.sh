openssl x509 -req -in remoteIxn/remote.csr -CA CA/ca.crt -CAkey CA/ca.key -CAcreateserial -out remoteIxn/remote.crt -days 365 -sha256
