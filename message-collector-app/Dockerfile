FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/message-collector-app-1.0.2-SNAPSHOT.jar message-collector.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null