FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/jms-client-image-sink-1.0.3-SNAPSHOT.jar jms-client-image-sink.jar
COPY entrypoint.sh /
RUN mkdir target
ENTRYPOINT ["/entrypoint.sh"]
