FROM eclipse-temurin:17-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

ARG JAR_FILE=target/*.jar
COPY $JAR_FILE jms-client-source-app.jar
RUN mkdir /work
WORKDIR /work
ENTRYPOINT ["java","-jar","/jms-client-source-app.jar"]