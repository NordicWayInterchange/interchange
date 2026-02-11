FROM eclipse-temurin:21-alpine
RUN apk add --no-cache bash alpine-conf \
    && setup-timezone -z Europe/Oslo

WORKDIR /app
ARG JAR_VERSION

COPY target/import-export-service-${JAR_VERSION}.jar import-export.jar

ENTRYPOINT ["java", "-jar", "/app/import-export.jar"]
