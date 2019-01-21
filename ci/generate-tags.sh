#!/bin/sh -eux

cd $(dirname $0)/..
mkdir -p ../tags

git rev-parse --short HEAD > ../tags/hash-tag
echo -n latest > ../tags/additional-tags

for tag in $(git --no-pager tags); do
    echo -n " ${tag}" >> ../tags/additional-tags
done

