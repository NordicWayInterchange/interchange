#!/bin/bash

java -jar jms-client-source-app.jar \
  -k $KEY_STORE_PATH \
  -s $KEY_STORE_PASS \
  -t $TRUST_STORE_PATH \
  -w $TRUST_STORE_PASS \
  $URL \
  $QUEUE "$@"