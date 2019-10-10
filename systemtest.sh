#!/bin/bash -eu

mvn package -PPKG
docker-compose -f systemtest.yml up --build