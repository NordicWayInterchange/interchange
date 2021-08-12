FROM java:8-alpine

RUN apk add --no-cache bash openssl jq

RUN rm -f /jks/keys/ready.txt

ADD entrypoint.sh /
ADD generate-keys.sh /scripts/

RUN chmod +x /entrypoint.sh
RUN chmod +x /scripts/generate-keys.sh

ENTRYPOINT ["/entrypoint.sh"]
