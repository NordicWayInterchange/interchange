#!/bin/bash -eu

echo "ENTRYPOINT - connecting to ${AMQP_URI} as ${AMQP_USER}"

LOG_LEVELS=${LOG_LEVELS:-" "}

java \
    -Damqphub.amqp10jms.remote-url=${AMQP_URI} \
    -Damqphub.amqp10jms.username=${AMQP_USER} \
    -Damqphub.amqp10jms.password=${AMQP_PASSWORD} \
    ${LOG_LEVELS} \
    -jar /interchange-node.jar