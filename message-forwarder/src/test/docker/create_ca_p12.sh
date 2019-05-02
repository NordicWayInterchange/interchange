#!/bin/bash
openssl pkcs12 -export -name ca -out CA/ca.p12 -inkey CA/ca.key -in CA/ca.crt -CAfile CA/ca.crt -password pass:password
