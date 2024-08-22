FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/onboard-rest-client-$JAR_VERSION.jar onboard-rest-client.jar

RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar", "/onboard-rest-client.jar"]
