#!/bin/bash
./generate_ca_key.sh
./create_ca_cert.sh
./create_ca_p12.sh
./create_ca_trust_store.sh
./generate_local_key.sh
./create_local_cert.sh
./create_local_trust_store.sh
./generate_local_p12.sh
./generate_remote_key.sh
./create_remote_cert.sh
./create_remote_trust_store.sh
./generate_remote_p12.sh
