#!/bin/bash
openssl x509 -req -in localIxn/local.csr -CA CA/ca.crt -CAkey CA/ca.key -CAcreateserial -out localIxn/local.crt -days 365 -sha256
