#!/bin/bash -eu


TMP_FOLDER=../keys/a
mkdir -p $TMP_FOLDER
java -jar ../../keys-generator/target/keys-generator-1.0.3-SNAPSHOT.jar generate -f ../keys/keys_a.json -o ../keys/a
