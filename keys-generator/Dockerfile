FROM eclipse-temurin:21-alpine

RUN apk add --no-cache bash openssl jq

COPY target/keys-generator-1.0.3-SNAPSHOT.jar /keys-generator.jar
COPY systemtest-keys.json /systemtest-keys.json

ENTRYPOINT ["java", "-jar", "/keys-generator.jar", "generate", "-f", "/systemtest-keys.json", "-o", "keys"]

