FROM alpine

RUN apk add --no-cache bash openssl

ADD signCSR.sh /
RUN chmod +x signCSR.sh
ENTRYPOINT ["/signCSR.sh"]
