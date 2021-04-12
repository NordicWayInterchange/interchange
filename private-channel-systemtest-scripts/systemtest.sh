#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Running system test on branch $BRANCH with tag $BRANCH_TAG

cd ../onboard-rest-client
docker build . -t onboard_rest_client
cd ../private-channel-systemtest-scripts
[ -f ../tmp/keys/local.bouvetinterchange.eu.p12 ] || ./systemtest-keys.sh
docker-compose -f systemtest.yml build && docker-compose -f systemtest.yml up
