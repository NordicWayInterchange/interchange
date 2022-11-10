FROM eclipse-temurin:11

RUN apk add --no-cache bash openssl jq

ENV QPID_VERSION=8.0.6

RUN cd /usr/local && \
    wget http://archive.apache.org/dist/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

ADD entrypoint.sh /
RUN chmod ug+x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]


