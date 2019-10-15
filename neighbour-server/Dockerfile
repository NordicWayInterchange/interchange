FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/neighbour-server-1.0-SNAPSHOT.jar neighbour-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]