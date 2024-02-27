#!/bin/bash

java -jar napcore-rest-client.jar \
  -e $KEY_STORE \
  -e $KEY_STORE_PASSWORD \
  -e $TRUST_STORE_PATH \
  -e $TRUST_STORE_PASSWORD \
  -e NAP_SERVER \
  -e USER \
