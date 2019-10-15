#!/bin/bash -eu

mvn package -PPKG
[ -f tmp/keys/remote.itsinterchange.eu.p12 ] || docker-compose -f systemtest-keys.yml up --build
docker-compose -f systemtest.yml up --build