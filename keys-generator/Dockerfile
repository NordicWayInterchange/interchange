FROM eclipse-temurin:21-alpine

RUN apk add --no-cache bash openssl jq

ARG JAR_VERSION

COPY target/keys-generator-$JAR_VERSION.jar /keys-generator.jar
COPY singlenode-keys.json /singlenode-keys.json

ENTRYPOINT ["java", "-jar", "/keys-generator.jar", "generate", "-f", "/singlenode-keys.json", "-o", "keys"]

