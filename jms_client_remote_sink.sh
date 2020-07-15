#!/bin/bash
docker run -it --rm --name jms_client_sink --network=interchange_testing_net --dns=172.28.1.1 -v /c/interchange/tmp/keys:/keys --link remote_qpid:remote.bouvetinterchange.eu jms_client_sink --entrypoint bash
