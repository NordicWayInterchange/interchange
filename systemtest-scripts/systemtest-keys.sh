#!/bin/bash -eu


TMP_FOLDER=$(realpath -s ../tmp)
echo Generating systemtest keys to folder $TMP_FOLDER

docker build ../key-gen -t key-gen
docker run -it -e CA_CN=ca.bouvetinterchange.eu -e KEY_CNS="a.bouvetinterchange.eu b.bouvetinterchange.eu c.bouvetinterchange.eu king_gustaf.bouvetinterchange.eu king_olav.bouvetinterchange.eu king_charles.bouvetinterchange.eu" -v $TMP_FOLDER/keys/:/jks/keys key-gen:latest

