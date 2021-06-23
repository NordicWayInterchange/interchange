FROM alpine

RUN apk add --no-cache bash openssl

ADD makeServerCert.sh /
RUN chmod +x makeServerCert.sh
ENTRYPOINT ["bash","-x", "/makeServerCert.sh"]
