#!/bin/bash
#openssl genrsa -out remoteIxn/remote.key 2048
openssl req -new -newkey rsa:2048 -nodes -keyout remoteIxn/remote.key -out remoteIxn/remote.csr -subj "/CN=remote/O=Nordic Way/C=NO"
