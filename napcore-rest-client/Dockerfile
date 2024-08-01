FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/napcore-rest-client-$JAR_VERSION.jar napcore-rest-client.jar

RUN mkdir /work
WORKDIR /work

ENTRYPOINT ["java", "-jar", "/napcore-rest-client.jar"]