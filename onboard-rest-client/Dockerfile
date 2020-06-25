FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/onboard-rest-client-1.0.3-SNAPSHOT.jar onboard-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ADD datatype.json /
ENTRYPOINT ["/entrypoint.sh"]
