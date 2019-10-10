#!/bin/bash -eu

POSTGIS_URI=${POSTGIS_URI:-jdbc:postgresql://postgis:5432/geolookup}

echo "ENTRYPOINT - connecting to ${AMQP_URI} as ${AMQP_USER} and to PGSQL server ${POSTGIS_URI}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java \
    -Damqphub.amqp10jms.remote-url=${AMQP_URI} \
    -Damqphub.amqp10jms.username=${AMQP_USER} \
    -Damqphub.amqp10jms.password=${AMQP_PASSWORD} \
    -Dspring.datasource.url=${POSTGIS_URI} \
    ${LOG_LEVELS} \
    -jar /interchange-node.jar