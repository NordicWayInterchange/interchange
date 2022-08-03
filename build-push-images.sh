#!/bin/bash -eu

echo Building and pushing docker images to container registry $1

REGISTRY=$1
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="qpid message-collector-app neighbour-discoverer-app neighbour-server-app routing-configurer-app onboard-server-app"
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

