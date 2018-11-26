FROM java:8-alpine

RUN apk update && apk add bash

ENV QPID_VERSION=7.0.6

RUN cd /usr/local && \
    wget http://apache.uib.no/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

ENV QPID_WORK=/work

EXPOSE 8080
EXPOSE 5671
EXPOSE 5672

ADD config.json /work/
ADD entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
