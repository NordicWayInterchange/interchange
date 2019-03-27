FROM federation_build as build

COPY neighbour-server/entrypoint.sh /entrypoint.sh
COPY --from=build neighbour-server/target/neighbour-server-1.0-SNAPSHOT.jar neighbour-server.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null