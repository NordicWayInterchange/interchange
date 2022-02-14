#!/bin/bash -eu

cd ../jms-client-sink
docker build . -t jms_client_sink
cd ../jms-client-source
docker build . -t jms_client_source


