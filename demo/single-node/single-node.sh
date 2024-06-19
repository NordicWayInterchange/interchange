#!/bin/bash -eu

export IXN_VERSION=$(<version)
docker-compose -f single-node.yml up --build
