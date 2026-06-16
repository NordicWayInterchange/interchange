FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf \
    && setup-timezone -z Europe/Oslo

WORKDIR /app
ARG JAR_VERSION

COPY target/export-application-${JAR_VERSION}.jar export.jar

ENTRYPOINT ["java", "-jar", "/app/export.jar"]
