#!/bin/bash
set -euo pipefail
# tear down anything left over from a previous run, then prune unused
# volumes/containers/networks. This runs both before starting (clean slate)
# and automatically when this script exits/is stopped (Ctrl+C, error, etc.).
cleanup() {
  echo "Cleaning up docker environment..."
  docker compose -f single-node.yml down --remove-orphans || true
  docker volume prune -f
  docker container prune -f
  docker network prune -f
}
trap cleanup EXIT INT TERM

cleanup

export IMAGE_TAG=$(<version)
VOL_EXISTS=$( docker volume ls --format '{{.Name}}' -f name=single-node-keys-volume)
[ -n "$VOL_EXISTS" ] || ./single-node-keys.sh
#[ -d ../keys/a/ ] || ./single-node-keys.sh
export NAP_TAG=$(<nap_version)
docker compose -f single-node.yml up --build

