#!/bin/bash

java -jar onboard-rest-client.jar \
  -k $KEY_STORE \
  -p $KEY_STORE_PASSWORD \
  -t $TRUST_STORE_PATH \
  -w $TRUST_STORE_PASSWORD \
  $ONBOARD_SERVER \
  $USER "$@"

