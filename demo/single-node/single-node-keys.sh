#!/bin/bash -eu

IMAGE_TAG=c0dbadb
KEYS_FOLDER=$(realpath ../keys/a)
echo Generating systemtest keys to folder $KEYS_FOLDER
mkdir -p $KEYS_FOLDER

docker run -it -v $KEYS_FOLDER:/keys --user=$(id -u):$(id -g) europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/keys-generator:${IMAGE_TAG} generate -f single-node-keys.json -o $KEYS_FOLDER
