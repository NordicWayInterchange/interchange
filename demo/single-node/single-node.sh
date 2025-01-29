#!/bin/bash -eu
if [ "$(git rev-parse --abbrev-ref HEAD)" == "federation-master" ]; then
        git rev-parse --short=7 HEAD > version
fi
[ -d ../keys/a/ ] || ./single-node-keys.sh
IMAGE_TAG=$(<version) docker-compose -f single-node.yml up --build

