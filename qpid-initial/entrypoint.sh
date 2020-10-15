#!/bin/bash


for entry in "$@"
do
    echo "$entry"
done
${QPID_HOME}/bin/qpid-server

