FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash

COPY target/neighbour-discoverer-app-1.0.3-SNAPSHOT.jar neighbour-discoverer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]