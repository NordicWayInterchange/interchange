FROM federation-build:latest as build

FROM java:8-alpine
RUN apk add --no-cache bash

COPY --from=build neighbour-discoverer/target/neighbour-discoverer-1.0-SNAPSHOT.jar neighbour-discoverer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]