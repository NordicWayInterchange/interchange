FROM alpine

RUN apk add --no-cache bash openssl

ADD rootca /rootca
RUN chmod +x /rootca/makeRootCA.sh
ENTRYPOINT ["/rootca/makeRootCA.sh"]