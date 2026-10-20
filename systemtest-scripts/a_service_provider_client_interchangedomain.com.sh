#!/bin/bash
# Example:
#   ./a_service_provider_client_interchangedomain.com.sh login \
#     --token-endpoint=http://a-keycloak:8080/realms/a.interchangedomain.com/protocol/openid-connect/token \
#     --client-id=a.interchangedomain.com-onboard --client-secret=anothersupersecret
#   ./a_service_provider_client_interchangedomain.com.sh capabilities list

SERVICE_PROVIDER="king_olav.a.interchangedomain.com"
URL="https://a.interchangedomain.com:8797/"
VOLUME_NAME=single-node-systemtest-keys-volume

docker run \
  -it \
  --rm \
  --network=systemtest-scripts_testing_net \
  --dns=172.26.1.1 \
  -v ${VOLUME_NAME}:/keys \
  -v $PWD:/work \
  -e SERVICEPROVIDERCLIENT_TOKEN_FILE=/work/.serviceproviderclient-token.json \
  --link a-onboard-server:a.interchangedomain.com \
  --link a-qpid:a.qpid.interchangedomain.com \
  service_provider_client -k /keys/${SERVICE_PROVIDER}.p12 -s password -t /keys/ca.interchangedomain.com.jks -w password $URL -u ${SERVICE_PROVIDER} "$@"

