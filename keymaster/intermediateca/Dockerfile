FROM alpine

RUN apk add --no-cache bash openssl

ADD makeIntermediateCaCSR.sh /
RUN chmod +x makeIntermediateCaCSR.sh
ENTRYPOINT ["/makeIntermediateCaCSR.sh"]