FROM federation_build as build

FROM java:8-alpine
RUN apk add --no-cache bash

COPY --from=build onboard-server/target/onboard-server-1.0-SNAPSHOT.jar onboard-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]