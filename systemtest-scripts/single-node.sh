#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Running system test on branch $BRANCH with tag $BRANCH_TAG

cd ../
mvn clean package
cd onboard-rest-client
docker build . -t onboard_rest_client
cd ../napcore-rest-client
docker build . -t napcore_rest_client
cd ../jms-client-source-app
docker build . -t jms_client_source_app
cd ../systemtest-scripts
[ -f ../tmp/keys/a.interchangedomain.com.p12 ] || ./keys-single-node.sh
docker-compose -f single-node-systemtest.yml build && docker-compose -f single-node-systemtest.yml up
