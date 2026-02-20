#!/bin/bash -eu

[ -d ../keys/a/ ] || ./single-node-keys.sh
export IMAGE_TAG=$(<version)
export NAP_TAG=$(<nap_version)
docker-compose -f single-node.yml up --build

