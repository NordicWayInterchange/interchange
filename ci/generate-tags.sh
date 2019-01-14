#!/bin/sh -eux

cd $(dirname $0)/..
mkdir -p ../tags
echo latest > ../tags/latest-tag
git rev-parse --short HEAD > ../tags/hash-tag

