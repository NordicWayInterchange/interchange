#!/bin/bash -eu


TMP_FOLDER=$(realpath -s ../tmp)
echo Generating systemtest keys to folder $TMP_FOLDER

mkdir -p $TMP_FOLDER/keys
docker build ../../key-gen -t key-gen
docker run -it --user=$UID -e CA_CN=dns.lookupdomain.com -e KEY_CNS="a.interchangedomain.com king_olav.a.interchangedomain.com" -v $TMP_FOLDER/keys/:/jks/keys key-gen:latest

