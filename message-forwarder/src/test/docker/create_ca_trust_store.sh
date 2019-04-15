#!/bin/bash
keytool -import -trustcacerts -alias ca -file CA/ca.crt -keystore CA/truststore.jks -storepass password -noprompt
