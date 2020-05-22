FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/interchange-node-app-1.0.1-SNAPSHOT.jar interchange-node.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
