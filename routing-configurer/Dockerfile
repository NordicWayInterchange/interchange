FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/routing-configurer-1.0-SNAPSHOT-exec.jar routing-configurer.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]