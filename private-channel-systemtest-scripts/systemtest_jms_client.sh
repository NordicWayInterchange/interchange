#!/bin/bash -eu

cd ../jms-client
mvn clean install
cd ../jms-client-sink
mvn clean package
docker build . -t jms_client_sink
cd ../jms-client-source
mvn clean package
docker build . -t jms_client_source


