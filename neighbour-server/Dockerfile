FROM maven:3.6.0-jdk-8-alpine as build

COPY pom.xml pom.xml
COPY src src

# RUN mvn clean install

FROM java:8

COPY entrypoint.sh /entrypoint.sh
# COPY --from=build target/neighbour-server-1.0-SNAPSHOT.jar neighbour-server.jar

#ENTRYPOINT ["/entrypoint.sh"]


CMD tail -f /dev/null