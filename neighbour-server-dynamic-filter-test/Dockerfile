FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY neighbour-server-dynamic-filter-test-${JAR_VERSION}.jar neighbour-server-dynamic-filter-test.jar

COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
