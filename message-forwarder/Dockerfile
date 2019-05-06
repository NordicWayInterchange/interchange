FROM federation-build:latest

COPY entrypoint.sh /entrypoint.sh
COPY --from=federation-build:latest message-forwarder/target/message-forwarder-1.0-SNAPSHOT.jar message-forwarder.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null