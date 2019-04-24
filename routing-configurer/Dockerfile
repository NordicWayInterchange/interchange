FROM federation-build:latest

COPY entrypoint.sh /entrypoint.sh
COPY --from=federation-build:latest routing-configurer/target/routing-configurer-1.0-SNAPSHOT.jar routing-configurer.jar

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null