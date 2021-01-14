#!/bin/bash -eu


TMP_FOLDER=../$(realpath -s tmp)
echo Generating systemtest keys to folder $TMP_FOLDER

docker build ../key-gen -t key-gen
docker run -it -e CA_CN=ca.bouvetinterchange.eu -e KEY_CNS="local.bouvetinterchange.eu remote.bouvetinterchange.eu king_gustaf.bouvetinterchange.eu" -v $TMP_FOLDER/keys/:/jks/keys key-gen:latest

