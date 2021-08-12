FROM adoptopenjdk/openjdk8:alpine
RUN apk add --no-cache bash

COPY target/dns-client-1.0.3-SNAPSHOT.jar dns-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
