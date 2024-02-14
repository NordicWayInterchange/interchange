#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Running system test on branch $BRANCH with tag $BRANCH_TAG
#TODO Don't build the packages, but use images from container registry.
#TODO How do we do this with regards to keys???
#cd ../../
#mvn clean package
#cd onboard-rest-client
#docker build . -t onboard_rest_client
#cd ../napcore-rest-client
#docker build . -t napcore_rest_client
#cd ../jms-client-source-app
#docker build . -t jms_client_source_app
#cd ../demo/single-node
[ -f ../tmp/keys/a.interchangedomain.com.p12 ] || ./keys-single-node.sh
docker-compose -f single-node.yml build && docker-compose -f single-node.yml up
