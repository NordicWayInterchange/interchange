FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash

COPY target/onboard-server-app-1.0.3-SNAPSHOT.jar onboard-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]