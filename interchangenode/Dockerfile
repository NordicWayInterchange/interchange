FROM federation-build:latest as build

FROM java:8-alpine
RUN apk add --no-cache bash

COPY --from=build interchangenode/target/interchange-node-1.0-SNAPSHOT.jar interchange-node.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
