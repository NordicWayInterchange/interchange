FROM adoptopenjdk/openjdk8:alpine
RUN apk add --no-cache bash

COPY target/jms-client-sink-1.0.3-SNAPSHOT.jar jms-client-sink.jar
COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
