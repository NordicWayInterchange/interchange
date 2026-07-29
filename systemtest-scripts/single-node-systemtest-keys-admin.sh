#!/bin/bash
set -euo pipefail

VOLUME_NAME=single-node-systemtest-keys-admin-volume

echo "Generating keys to volume $VOLUME_NAME"
docker volume create $VOLUME_NAME
docker run -it -v${PWD}:/work -v $VOLUME_NAME:/keys keys-generator generate -s -f single-node-systemtest-keys-admin.json -o /keys
