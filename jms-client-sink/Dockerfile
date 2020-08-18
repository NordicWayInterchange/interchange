FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/jms-client-sink-1.0.3-SNAPSHOT.jar jms-client-sink.jar
COPY src/main/resources/sink.properties sink.properties
ENTRYPOINT ["java","-jar","jms-client-sink.jar","sink.properties"]
