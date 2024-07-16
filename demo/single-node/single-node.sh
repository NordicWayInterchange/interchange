#!/bin/bash -eu

mkdir -p ../keys
cd ../../keys-generator
docker build . -t keys
cd -
docker run --rm -v $PWD/../keys:/keys keys
IMAGE_TAG=3743e41 docker-compose -f single-node.yml up --build

