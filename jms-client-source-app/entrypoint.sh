#!/bin/bash

java -jar jms-client-source-app.jar \
  -k $KEY_STORE \
  -p $KEY_STORE_PASSWORD \
  -s $KEY_PASSWORD \
  -t $TRUST_STORE_PATH \
  -w $TRUST_STORE_PASSWORD \
  $URL \
  $QUEUE "$@"