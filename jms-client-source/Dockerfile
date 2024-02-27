FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/jms-client-source-1.0.3-SNAPSHOT.jar jms-client-source.jar
COPY entrypoint.sh /entrypoint.sh
COPY cabin_view.jpg /cabin_view.jpg
ENTRYPOINT ["/entrypoint.sh"]
