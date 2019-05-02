#!/bin/bash
#openssl genrsa -out localIxn/local.key 2048
#openssl req -new -newkey rsa:2048 -nodes -keyout localIxn/local.key -out localIxn/local.csr
openssl req -new -newkey rsa:2048 -nodes -keyout localIxn/local.key -out localIxn/local.csr -subj "/CN=local/O=Nordic Way/C=NO"
