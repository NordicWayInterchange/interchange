FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/napcore-rest-client-1.0.3-SNAPSHOT.jar napcore-rest-client.jar

RUN mkdir /work
WORKDIR /work

ENTRYPOINT ["java", "-jar", "/napcore-rest-client.jar"]