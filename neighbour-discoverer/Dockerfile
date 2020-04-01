FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/neighbour-discoverer-1.0-SNAPSHOT-exec.jar neighbour-discoverer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]