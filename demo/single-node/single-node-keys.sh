#!/bin/bash -eu
VOLUME_NAME=single-node-keys-volume
echo "Generating systemtest keys to volume $VOLUME_NAME"
docker volume create $VOLUME_NAME
docker run -it -v ${PWD}:/work -v $VOLUME_NAME:/keys \
	ghcr.io/nordicwayinterchange/keys-generator:${IMAGE_TAG} generate -f single-node-keys.json -o /keys
