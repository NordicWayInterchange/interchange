FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/jms-client-source-app-1.0.3-SNAPSHOT.jar jms-client-source-app.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh /
ADD message_king_olav.json /
ADD messages_king_olav.json /
ENTRYPOINT ["/entrypoint.sh"]