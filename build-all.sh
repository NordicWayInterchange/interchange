#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
PUSH=${PUSH:-false}
IMAGES="interchangenode qpid postgis"

for image in ${IMAGES}; do
    pushd ${image}
    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${image}:latest
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:latest
    if [[ ${PUSH} == true ]]; then
        docker push ${REGISTRY}/${image}:${TAG}
        docker push ${REGISTRY}/${image}:latest
    fi
    popd
done

for image in ${IMAGES}; do
    echo "${REGISTRY}/${image}:${TAG}"
done
