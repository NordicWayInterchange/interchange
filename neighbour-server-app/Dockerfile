FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/neighbour-server-app-1.0.3-SNAPSHOT.jar neighbour-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]