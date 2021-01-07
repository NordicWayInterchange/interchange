#!/bin/bash -eu

cd ..
cd jms-client
mvn clean install
cd ..
cd jms-client-sink
mvn clean package
docker build . -t jms_client_sink
cd ..
cd jms-client-source
mvn clean package
docker build . -t jms_client_source


