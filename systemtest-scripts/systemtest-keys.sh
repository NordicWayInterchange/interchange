#!/bin/bash -eu

VOLUME_NAME=systemtest-keys-volume

echo "Generating keys to volume $VOLUME_NAME"
docker volume create $VOLUME_NAME
docker run -it -v${PWD}:/work -v $VOLUME_NAME:/keys keys-generator generate -f systemtest-keys.json -o /keys
