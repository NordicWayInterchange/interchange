#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
export SYSTEMTEST_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
echo Running system test on branch $SYSTEMTEST_BRANCH

[ -f tmp/keys/remote.itsinterchange.eu.p12 ] || docker-compose -f systemtest-keys.yml up --build
docker-compose -f systemtest.yml up --build
