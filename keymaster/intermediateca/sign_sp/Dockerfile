FROM alpine

RUN apk add --no-cache bash openssl

ADD makeClientCert.sh /
RUN chmod +x makeClientCert.sh
ENTRYPOINT ["/makeClientCert.sh"]