FROM federation-build:latest

COPY entrypoint.sh /entrypoint.sh
COPY --from=federation-build:latest neighbour-server/target/neighbour-server-1.0-SNAPSHOT.jar neighbour-server.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null