FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/napcore-rest-client-1.0.3-SNAPSHOT.jar napcore-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]