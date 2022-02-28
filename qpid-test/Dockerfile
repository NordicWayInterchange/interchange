FROM adoptopenjdk/openjdk8:alpine

RUN apk add --no-cache bash openssl jq

ENV QPID_VERSION=8.0.6

RUN cd /usr/local && \
    wget http://archive.apache.org/dist/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

RUN mkdir -p /work
ENV QPID_WORK="/work"
ENV QPID_HOME="/usr/local/qpid"
ENV VHOST_FILE="file:/config/vhost.json"
ENV PASSWD_FILE="/config/passwd"
ENV GROUPS_FILE="/config/groups"
ENV QPID_OPTS="-Djdk.tls.client.protocols=\"TLSv1.3\""
ADD config.json ${QPID_WORK}
ADD entrypoint.sh /
RUN chmod ug+x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]


