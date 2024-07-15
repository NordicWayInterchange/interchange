FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/napcore-rest-client-$JAR_VERSION.jar napcore-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh

ADD sub_king_olav.json /

ENTRYPOINT ["/entrypoint.sh"]