FROM data-helper-build:latest as build


FROM java:8-alpine
RUN apk add --no-cache bash

COPY --from=build test-data-helpers/target/test-data-helpers-1.0-SNAPSHOT.jar test-data-helpers.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]