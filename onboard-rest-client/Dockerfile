FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/onboard-rest-client-1.0.3-SNAPSHOT.jar onboard-rest-client.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar", "/onboard-rest-client.jar"]
