#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
PUSH=${PUSH:-false}
IMAGES="interchangenode qpid postgis message-forwarder neighbour-discoverer neighbour-server routing-configurer"

docker build -f federation-docker-files/Federation_build -t federation-build:${TAG} .
docker tag federation-build:${TAG} federation-build:latest

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${image}:latest
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:fed_latest
    if [[ ${PUSH} == true ]]; then
        docker push ${REGISTRY}/${image}:${TAG}
        docker push ${REGISTRY}/${image}:fed_latest
    fi
    popd
done

echo ""
for image in ${IMAGES}; do
    echo "${REGISTRY}/${image}:${TAG}"
done
