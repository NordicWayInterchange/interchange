FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash

ADD createTruststore.sh /

ENTRYPOINT ["/createTruststore.sh"]
