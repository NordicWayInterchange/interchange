FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash

ADD createTruststore.sh /
RUN chmod +x /createTruststore.sh

ENTRYPOINT ["/createTruststore.sh"]
