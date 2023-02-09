FROM alpine

RUN apk add --no-cache bash openssl

ADD createKeyStore.sh /

ENTRYPOINT ["/createKeyStore.sh"]