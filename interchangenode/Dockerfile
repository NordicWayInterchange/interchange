FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/interchange-node-1.0-SNAPSHOT.jar interchange-node.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
