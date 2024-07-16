#!/bin/bash -eu

cd ../../keys-generator/Dockerfile
docker build -f ../../keys-generator/Dockerfile
cd -
docker-compose -f single-node.yml up --build

