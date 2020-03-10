FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/dns-client-1.0-SNAPSHOT-jar-with-dependencies.jar dns-client.jar

ENTRYPOINT java -jar dns-client.jar