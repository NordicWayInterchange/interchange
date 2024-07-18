#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Running system test on branch $BRANCH with tag $BRANCH_TAG


docker build ../onboard-rest-client -t onboard_rest_client --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
docker build ../napcore-rest-client -t napcore_rest_client --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
docker build ../jms-client-app -t jms_client --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
[ -f ../tmp/keys/a.bouvetinterchange.eu.p12 ] || ./systemtest-keys.sh
docker-compose -f systemtest.yml build --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout) && docker-compose -f systemtest.yml up
