#!/bin/bash
openssl pkcs12 -export -out remoteIxn/remote.p12 -inkey remoteIxn/remote.key -in remoteIxn/remote.crt -CAfile CA/ca.crt -password pass:password
