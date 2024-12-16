#!/bin/bash -eu

#Set environment variable to choose what docker image tag to use in the test
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

echo Running system test on branch $BRANCH with tag $BRANCH_TAG

cd ../service-provider-client
docker build . -t service-provider-client --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
cd ../systemtest-scripts
[ -f ../tmp/keys/b.bouvetinterchange.eu.p12 ] || ./systemtest-keys.sh
docker-compose -f new-systemtest.yml build --build-arg JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)  && docker-compose -f new-systemtest.yml up
