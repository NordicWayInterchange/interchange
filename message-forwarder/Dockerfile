FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/message-forwarder-1.0-SNAPSHOT.jar message-forwarder.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null