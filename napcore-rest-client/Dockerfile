FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/napcore-rest-client-1.0.3-SNAPSHOT.jar napcore-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh

ADD sub_king_olav.json /

ENTRYPOINT ["/entrypoint.sh"]