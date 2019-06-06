#!/usr/bin/env bash

echo "ENTRYPOINT - connecting to PGSQL server ${POSTGRES_URI}"
echo "ENTRYPOINT - using db helper type: ${DB_HELPER_TYPE}"

java -Dspring.datasource.url=${POSTGRES_URI} \
     -Ddb-helper.type=${DB_HELPER_TYPE} \
     -jar test-data-helpers.jar