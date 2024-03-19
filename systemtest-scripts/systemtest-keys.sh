#!/bin/bash -eu


TMP_FOLDER=../tmp/keys/
echo Generating systemtest keys to folder $TMP_FOLDER
mkdir -p $TMP_FOLDER

REALFOLDER=$(realpath -s $TMP_FOLDER)

docker build ../key-gen -t key-gen
docker run -it --user=$(id -u):$(id -g) -e CA_CN=ca.bouvetinterchange.eu -e KEY_CNS="nap.bouvetinterchange.eu a.bouvetinterchange.eu b.bouvetinterchange.eu c.bouvetinterchange.eu king_gustaf.bouvetinterchange.eu king_olav.bouvetinterchange.eu king_charles.bouvetinterchange.eu a_qpid a_routing_configurer b_qpid b_routing_configurer" -v $REALFOLDER:/jks/keys key-gen:latest

