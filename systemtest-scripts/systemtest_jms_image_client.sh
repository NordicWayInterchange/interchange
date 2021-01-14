#!/bin/bash -eu

cd ../jms-client
mvn clean install
cd ../jms-client-image-sink
mvn clean package
docker build . -t jms_client_image_sink
cd ../jms-client-image-source
mvn clean package
docker build . -t jms_client_image_source