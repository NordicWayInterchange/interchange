#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="interchange-node-app qpid message-collector-app neighbour-discoverer-app neighbour-server-app routing-configurer onboard-server"
BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}
    docker push ${REGISTRY}/${image}:${TAG}

    docker tag ${image}:${TAG} ${image}:${BRANCH_TAG}
    docker tag ${image}:${BRANCH_TAG} ${REGISTRY}/${image}:${BRANCH_TAG}
    docker push ${REGISTRY}/${image}:${BRANCH_TAG}
    popd
done

