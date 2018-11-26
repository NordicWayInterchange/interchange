#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
PUSH=${PUSH:-false}
IMAGES="interchangenode qpid postgis"

echo "make sure you've run 'mvn clean install' in interchangenodeapp. Build depends on artefacts outputed from that command."
echo ""

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

echo ""
for image in ${IMAGES}; do
    echo "${REGISTRY}/${image}:${TAG}"
done
