#!/bin/bash -eu

TAG="$(git rev-parse --short HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
IMAGES="interchangenode qpid message-collector neighbour-discoverer neighbour-server routing-configurer onboard-server"

mvn clean install -PPKG

for image in ${IMAGES}; do
    pushd ${image}

    docker build -t ${image}:${TAG} .
    docker tag ${image}:${TAG} ${image}:${BRANCH}

    popd
done

echo ""
for image in ${IMAGES}; do
    echo "${image}:${TAG}"
    echo "${image}:${BRANCH}"
done
