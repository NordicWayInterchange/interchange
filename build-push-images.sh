#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="interchangenode qpid"

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}
    docker push ${REGISTRY}/${image}:${TAG}

    docker tag ${image}:${TAG} ${image}:${BRANCH}
    docker tag ${image}:${BRANCH} ${REGISTRY}/${image}:${BRANCH}
    docker push ${REGISTRY}/${image}:${BRANCH}
    popd
done
