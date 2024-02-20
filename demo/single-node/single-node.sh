#!/bin/bash -eu

docker-compose -f single-node.yml build && docker-compose -f single-node.yml up
