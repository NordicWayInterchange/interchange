FROM alpine

RUN apk add --no-cache bash openssl

ADD createKeyStore.sh /
RUN chmod +x createKeyStore.sh

ENTRYPOINT ["/createKeyStore.sh"]