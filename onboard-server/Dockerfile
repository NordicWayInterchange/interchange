FROM federation_build as build

COPY onboard-server/entrypoint.sh /entrypoint.sh
COPY --from=build onboard-server/target/onboard-server-1.0-SNAPSHOT.jar onboard-server.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null