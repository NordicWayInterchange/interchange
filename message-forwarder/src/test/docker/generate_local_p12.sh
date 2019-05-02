#!/bin/bash
openssl pkcs12 -export -out localIxn/local.p12 -inkey localIxn/local.key -in localIxn/local.crt -CAfile CA/ca.crt -password pass:password
