#!/bin/bash -eu


REGISTRY=$1
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="interchange-node-app qpid message-collector-app neighbour-discoverer-app neighbour-server-app routing-configurer-app onboard-server-app"
BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Building docker images with tags: $TAG, $BRANCH_TAG

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}

    docker tag ${image}:${TAG} ${image}:${BRANCH_TAG}
    docker tag ${image}:${BRANCH_TAG} ${REGISTRY}/${image}:${BRANCH_TAG}
    popd
done

