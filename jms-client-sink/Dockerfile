FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/jms-client-sink-1.0.3-SNAPSHOT.jar jms-client-sink.jar
COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
