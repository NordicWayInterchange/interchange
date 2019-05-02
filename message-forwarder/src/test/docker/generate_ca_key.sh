#!/bin/bash
openssl req -new -newkey rsa:2048 -nodes -keyout CA/ca.key -out CA/ca.csr -subj "/CN=cert_auth/O=Nordic Way/C=NO"
