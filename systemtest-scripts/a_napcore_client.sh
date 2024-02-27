#!/bin/bash


docker run \
-it \
--rm \
--name a_napcore_client \
--network=systemtest-scripts_testing_net \
--dns=172.28.1.1 \
-v $PWD/../tmp/keys:/keys \
-e KEY_STORE=/keys/${} \
-e KEY_STORE_PASSWORD=password \
-e TRUST_STORE_PATH=/keys/truststore.jks \
-e TRUST_STORE_PASSWORD=password \
-e NAPCORE_SERVER=https://a.bouv