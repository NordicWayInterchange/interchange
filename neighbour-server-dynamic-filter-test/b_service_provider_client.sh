#!/bin/bash -eux

SERVICE_PROVIDER="king_gustaf.b.bouvetinterchange.eu"
URL="https://b.bouvetinterchange.eu:8696/"


docker run \
  -it \
  --rm \
  --network=neighbour-client_testing_net \
  --dns=172.28.1.1 \
  -v $PWD/../../interchange/tmp/keys:/keys \
  -v $PWD:/work \
  --link b_qpid:b.qpid.bouvetinterchange.eu \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.bouvetinterchange.eu.jks  -w password amqps://b.qpid.bouvetinterchange.eu:5671 -u ${SERVICE_PROVIDER} messages send test-queue -f "$1"
