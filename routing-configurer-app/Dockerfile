FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/routing-configurer-app-1.0.2-SNAPSHOT.jar routing-configurer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]