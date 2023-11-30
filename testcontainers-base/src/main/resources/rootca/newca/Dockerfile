FROM eclipse-temurin:17-alpine

RUN apk add --no-cache bash openssl

RUN mkdir /rootca
ADD makeRootCA.sh /rootca/
RUN chmod +x /rootca/makeRootCA.sh
ENTRYPOINT ["/rootca/makeRootCA.sh"]