FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/jms-client-source-app-$JAR_VERSION.jar jms-client-source-app.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar","/jms-client-source-app.jar"]