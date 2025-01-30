#!/bin/bash -eu

[ -d ../keys/a/ ] || ./single-node-keys.sh
IMAGE_TAG=$(<version) docker-compose -f single-node.yml up --build

