FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/napcore-server-1.0.4-SNAPSHOT.jar napcore-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]