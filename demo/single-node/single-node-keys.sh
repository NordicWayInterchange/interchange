#!/bin/bash -eu

IMAGE_TAG=9c1c1e9
RELATIVE_KEYS_FOLDER=../keys/a
mkdir -p $RELATIVE_KEYS_FOLDER
KEYS_FOLDER=$(realpath ${RELATIVE_KEYS_FOLDER})
echo Generating systemtest keys to folder $KEYS_FOLDER

docker run -it -v ${PWD}:/work -v $KEYS_FOLDER:/keys --user=$(id -u):$(id -g) \
	europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/keys-generator:${IMAGE_TAG} generate -f single-node-keys.json -o /keys
