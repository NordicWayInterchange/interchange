FROM federation-build:latest as build

FROM java:8-alpine
RUN apk update && apk add bash

COPY --from=build routing-configurer/target/routing-configurer-1.0-SNAPSHOT.jar routing-configurer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

#CMD tail -f /dev/null