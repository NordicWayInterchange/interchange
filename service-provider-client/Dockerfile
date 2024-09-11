FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/service-provider-client-$JAR_VERSION.jar service-provider-client.jar

RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar", "/service-provider-client.jar"]
