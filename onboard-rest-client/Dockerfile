FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/onboard-rest-client-1.0-SNAPSHOT-jar-with-dependencies.jar onboard-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
