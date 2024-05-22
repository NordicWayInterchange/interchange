FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/jms-client-image-sink-1.0.3-SNAPSHOT.jar jms-client-image-sink.jar
COPY entrypoint.sh /
RUN mkdir target
ENTRYPOINT ["/entrypoint.sh"]
