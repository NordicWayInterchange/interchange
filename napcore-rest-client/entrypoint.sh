#!/bin/bash

java -jar napcore-rest-client.jar \
  -k $KEY_STORE \
  -s $KEY_STORE_PASSWORD \
  -t $TRUST_STORE_PATH \
  -w $TRUST_STORE_PASSWORD \
  $NAPCORE_SERVER \
  $USER "$@"