FROM alpine

RUN apk add --no-cache bash openssl

ADD makeClientCSR.sh /
RUN chmod +x makeClientCSR.sh
ENTRYPOINT ["/makeClientCSR.sh"]