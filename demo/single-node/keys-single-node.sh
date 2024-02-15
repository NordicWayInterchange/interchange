#!/bin/bash -eu


TMP_FOLDER=$(realpath -s ../tmp)
echo Generating systemtest keys to folder $TMP_FOLDER

docker build ../../key-gen -t key-gen
docker run -it -e CA_CN=dns.lookupdomain.com -e KEY_CNS="nap.a.interchangedomain.com a.interchangedomain.com king_olav.a.interchangedomain.com" -v $TMP_FOLDER/keys/:/jks/keys key-gen:latest

