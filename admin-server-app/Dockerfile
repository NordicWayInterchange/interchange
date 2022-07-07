FROM adoptopenjdk/openjdk8:alpine
RUN apk add --no-cache bash

COPY target/admin-server-app-1.0.3-SNAPSHOT.jar admin-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]