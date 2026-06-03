#!/bin/bash
set -euo pipefail

VOLUME_NAME=systemtest-keys-volume

echo "Generating keys to volume $VOLUME_NAME"
docker volume create $VOLUME_NAME
docker run -it -v${PWD}:/work -v $VOLUME_NAME:/keys keys-generator generate -s -f single-node-systemtest-keys.json -o /keys
