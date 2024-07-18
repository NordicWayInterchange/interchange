FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_VERSION
COPY target/jms-client-sink-app-$JAR_VERSION.jar jms-client-sink-app.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java", "-jar","/jms-client-sink-app.jar"]
