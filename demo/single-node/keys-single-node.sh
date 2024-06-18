#!/bin/bash -eu


TMP_FOLDER=../keys/a
mkdir -p $TMP_FOLDER
#REAL_FOLDER=$(realpath -s $TMP_FOLDER)
#echo Generating systemtest keys to folder $REAL_FOLDER

#docker build ../../key-gen -t key-gen
#docker run -it --user=$(id -u):$(id -g) -e CA_CN=dns.lookupdomain.com -e KEY_CNS="a.interchangedomain.com king_olav.a.interchangedomain.com" -v $REAL_FOLDER:/jks/keys key-gen:latest
java -jar ../../keys-generator/target/keys-generator-1.0.3-SNAPSHOT.jar generate -f ../keys/keys_a.json -o ../keys/a
