#!/bin/bash -eu

cd ../
mvn clean package -pl :jms-client-sink,:jms-client-source -am
cd jms-client-sink
docker build . -t jms_client_sink
cd ../jms-client-source
docker build . -t jms_client_source


