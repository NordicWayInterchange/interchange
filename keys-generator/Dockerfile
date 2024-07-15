FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/keys-generator-1.0.3-SNAPSHOT.jar keys-generator.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java", "-jar", "/keys-generator.jar"]