#!/bin/bash
set -euo pipefail


cleanup() {
  echo "Cleaning up docker environment..."
  docker-compose -f single-node.yml down --volumes
  docker volume rm single-node-keys-volume
}

trap cleanup EXIT INT TERM

export IMAGE_TAG=$(<version)
VOL_EXISTS=$( docker volume ls --format '{{.Name}}' -f name=single-node-keys-volume)
[ -n "$VOL_EXISTS" ] || ./single-node-keys.sh
export NAP_TAG=$(<nap_version)
docker compose -f single-node.yml up --build

