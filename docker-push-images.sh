#!/bin/bash -eu

echo Pushing docker images to container registry $1

REGISTRY=$1
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="interchange-node-app qpid message-collector-app neighbour-discoverer-app neighbour-server-app routing-configurer-app onboard-server-app"
BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

for image in ${IMAGES}; do
    pushd ${image}
    docker push ${REGISTRY}/${image}:${TAG}
    docker push ${REGISTRY}/${image}:${BRANCH_TAG}
    popd
done

