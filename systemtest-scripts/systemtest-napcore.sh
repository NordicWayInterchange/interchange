#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"
if [ ! -f napcoresettings ]; then
        echo "napcoresettings file does not exist. See napcoresettings.example, and fill in with your actual Aut0 settings"
        exit 1
fi
. napcoresettings
echo Running system test on branch $BRANCH with tag $BRANCH_TAG

docker build ../onboard-rest-client -t onboard_rest_client
docker build ../napcore-rest-client -t napcore_rest_client
docker build ../jms-client-app -t jms_client
[ -f ../tmp/keys/a.bouvetinterchange.eu.p12 ] || ./systemtest-keys.sh
docker-compose -f systemtest.yml -f systemtest-napcore.yml build && docker-compose -f systemtest.yml -f systemtest-napcore.yml up
