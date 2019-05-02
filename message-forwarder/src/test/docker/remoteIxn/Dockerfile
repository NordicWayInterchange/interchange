FROM java:8-alpine

RUN apk update && apk add bash openssl jq

ENV QPID_VERSION=7.0.6

RUN cd /usr/local && \
    wget http://archive.apache.org/dist/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

ENV QPID_WORK=/work

ADD config.json /work/
ADD entrypoint.sh /
#ADD generate-keys.sh /scripts/
ADD config /config
ADD remote.crt /keys/
ADD remote.key /keys/
ADD ca.crt /keys/

ENV CERTIFICATE_FILE "/keys/remote.crt"
ENV CA_CERT_FILE "/keys/ca.crt"
ENV PRIVATE_KEY_FILE "/keys/remote.key"
ENV VHOST_FILE "/config/vhost.json"
ENV GROUPS_FILE "/config/groups"
ENV PASSWD_FILE "/config/passwd"
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh","dev"]

HEALTHCHECK --interval=5s --timeout=3s CMD nc -zv localhost 5671

