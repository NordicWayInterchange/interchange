FROM eclipse-temurin:21-alpine

RUN apk add --no-cache bash openssl jq

COPY target/keys-generator-1.0.3-SNAPSHOT.jar /keys-generator.jar
COPY singlenode-keys.json /singlenode-keys.json

ENTRYPOINT ["java", "-jar", "/keys-generator.jar", "generate", "-f", "/singlenode-keys.json", "-o", "keys"]

