#!/bin/bash -eu

cd ..
cd jms-client
mvn clean install
cd ..
cd jms-client-image-sink
mvn clean package
docker build . -t jms_client_image_sink
cd ..
cd jms-client-image-source
mvn clean package
docker build . -t jms_client_image_source