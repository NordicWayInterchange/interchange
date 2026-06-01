#!/bin/bash
set -euo pipefail

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
export BRANCH_TAG="${BRANCH//[^a-zA-Z_0-9]/_}"

export JAR_VERSION=$(mvn -f .. org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
VOLUME_NAME=single-node-systemtest-keys-volume
docker build ../service-provider-client -t service_provider_client --build-arg JAR_VERSION=$JAR_VERSION
docker build ../napcore-rest-client -t napcore_rest_client --build-arg JAR_VERSION=$JAR_VERSION
docker build ../keys-generator -t keys-generator --build-arg JAR_VERSION=$JAR_VERSION
VOL_EXISTS=$( docker volume ls --format '{{.Name}}' -f name=${VOLUME_NAME})
[ -n "$VOL_EXISTS" ] || ./systemtest-keys.sh
docker compose -f single-node-systemtest.yml build --build-arg JAR_VERSION=$JAR_VERSION && docker compose -f single-node-systemtest.yml up

