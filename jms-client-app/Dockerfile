FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/jms-client-app-1.0.3-SNAPSHOT.jar jms-client-app.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java", "-jar","/jms-client-app.jar"]
