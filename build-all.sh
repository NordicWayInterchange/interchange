#!/bin/bash -eu

REGISTRY=eu.gcr.io/nordic-way-aad182cc
TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
PUSH=${PUSH:-false}
IMAGES="interchangenode qpid postgis message-forwarder neighbour-discoverer neighbour-server routing-configurer onboard-server"

mvn clean install -PPKG

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${REGISTRY}/${image}:${TAG}

    docker tag ${image}:${TAG} ${image}:${BRANCH}
    docker tag ${image}:${BRANCH} ${REGISTRY}/${image}:${BRANCH}

    if [[ ${PUSH} == true ]]; then
        docker push ${REGISTRY}/${image}:${TAG}
        docker push ${REGISTRY}/${image}:${BRANCH}
    fi
    popd
done

echo ""
for image in ${IMAGES}; do
    echo "${REGISTRY}/${image}:${TAG}"
    echo "${REGISTRY}/${image}:${BRANCH}"
done
