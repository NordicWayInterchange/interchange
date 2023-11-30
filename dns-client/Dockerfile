FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/dns-client-1.0.3-SNAPSHOT.jar dns-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
