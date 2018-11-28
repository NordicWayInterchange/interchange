FROM java:8

ADD entrypoint.sh /entrypoint.sh
ADD target/interchange-node-1.0-SNAPSHOT.jar ./interchange-node.jar

ENTRYPOINT ["/entrypoint.sh"]
