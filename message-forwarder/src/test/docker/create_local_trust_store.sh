#!/bin/bash
keytool -import -trustcacerts -file CA/ca.crt -keystore localIxn/truststore.jks -storepass password -noprompt
