#!/bin/bash -eu
export IMAGE_TAG=$(<version)
VOL_EXISTS=$( docker volume ls --format '{{.Name}}' -f name=single-node-keys-volume)
[ -n "$VOL_EXISTS" ] || ./single-node-keys.sh
#[ -d ../keys/a/ ] || ./single-node-keys.sh
export NAP_TAG=$(<nap_version)
docker-compose -f single-node.yml up --build

