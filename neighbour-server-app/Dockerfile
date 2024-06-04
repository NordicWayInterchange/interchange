FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_FILE=target/*.jar
COPY $JAR_FILE neighbour-server.jar

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]