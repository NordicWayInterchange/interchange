FROM maven:3.6.0-jdk-8 as build

COPY pom.xml pom.xml
COPY src src

RUN mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -B

FROM java:8

COPY entrypoint.sh /entrypoint.sh
COPY --from=build target/interchange-node-1.0-SNAPSHOT.jar interchange-node.jar

ENTRYPOINT ["/entrypoint.sh"]
