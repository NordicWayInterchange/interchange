#!/bin/bash

cd /Users/anna.fossen-helle/git/interchange/jms-client-source-app

java -jar target/jms-client-source-app-1.0.3-SNAPSHOT.jar \
        -k ../tmp/keys/king_olav.bouvetinterchange.eu.p12 \
        -s password \
        -p password\
        -t ../tmp/keys/truststore.jks \
        -w password \
        amqps://local.bouvetinterchange.eu \
        onramp \
        this is a message \
        king_olav.bouvetinterchange.eu \
        NO12345 \
        DENM \
        NO \
        DENM:1.2.2 \
        ,12003 \
        ${1}