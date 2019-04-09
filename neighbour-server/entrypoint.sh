#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Dinterchange.node-provider.name=${SERVER_NAME} \
     -jar neighbour-server.jar
