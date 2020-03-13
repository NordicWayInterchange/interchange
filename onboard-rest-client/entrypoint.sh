#!/bin/bash

echo java -jar onboard-rest-client.jar $ONBOARD_SERVER $USER $KEY_STORE $KEY_STORE_PASSWORD $KEY_PASSWORD
java -jar onboard-rest-client.jar $ONBOARD_SERVER $USER $KEY_STORE $KEY_STORE_PASSWORD $KEY_PASSWORD

