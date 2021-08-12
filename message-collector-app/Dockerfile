FROM adoptopenjdk/openjdk8:alpine
RUN apk add --no-cache bash

COPY target/message-collector-app-1.0.3-SNAPSHOT.jar message-collector.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null