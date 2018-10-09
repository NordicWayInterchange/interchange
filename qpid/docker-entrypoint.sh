#!/bin/bash

/usr/local/qpid-broker/7.0.6/bin/qpid-server -prop "qpid.amqp_port=5672" -prop "qpid.a_amqp_port=5673" -prop "qpid.http_port=8080"
