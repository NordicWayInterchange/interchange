FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash

COPY target/jms-client-source-app-1.0.3-SNAPSHOT.jar jms-client-source-app.jar
COPY entrypoint.sh /entrypoint.sh
RUN chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]