#!/bin/bash -eu


TMP_FOLDER=../tmp/keys/
echo Generating systemtest keys to folder $TMP_FOLDER
mkdir -p $TMP_FOLDER

java -jar ../keys-generator/target/keys-generator-1.0.3-SNAPSHOT.jar generate -f systemtest-keys.json -o "$TMP_FOLDER"
