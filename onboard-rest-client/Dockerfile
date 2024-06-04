FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_FILE=target/*.jar
COPY $JAR_FILE onboard-rest-client.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar", "/onboard-rest-client.jar"]
