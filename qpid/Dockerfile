FROM java:8-alpine

RUN apk update && apk add bash openssl

ENV QPID_VERSION=7.0.6

RUN cd /usr/local && \
    wget http://apache.uib.no/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

ENV QPID_WORK=/work

ADD config.json /work/
ADD entrypoint.sh /
ADD generate-keys.sh /scripts/

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

HEALTHCHECK --interval=5s --timeout=3s CMD nc -zv localhost 5671
