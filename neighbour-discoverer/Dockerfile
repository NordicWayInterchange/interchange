FROM federation_build as build

COPY neighbour-discoverer/entrypoint.sh /entrypoint.sh
COPY --from=build neighbour-discoverer/target/neighbour-discoverer-1.0-SNAPSHOT.jar neighbour-discoverer.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null