#!/bin/bash -e

REGISTRY=eu.gcr.io/nordic-way-aad182cc
IMAGE="${PWD##*/}"
TAG="$(git rev-parse --short HEAD)"

docker build -t ${IMAGE}:${TAG} .
docker tag ${IMAGE}:${TAG} ${IMAGE}:latest
docker tag ${IMAGE}:${TAG} ${REGISTRY}/${IMAGE}:${TAG}
docker tag ${IMAGE}:${TAG} ${REGISTRY}/${IMAGE}:latest

if [[ ${PUSH} == true ]]; then
    docker push ${REGISTRY}/${IMAGE}:${TAG}
    docker push ${REGISTRY}/${IMAGE}:latest
fi

echo "${REGISTRY}/${IMAGE}:${TAG}"
