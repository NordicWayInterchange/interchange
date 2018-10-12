FROM java:8

RUN cd /usr/local \
	&& wget http://apache.uib.no/qpid/broker-j/7.0.6/binaries/apache-qpid-broker-j-7.0.6-bin.tar.gz \
	&& tar -xvf apache-qpid-broker-j-7.0.6-bin.tar.gz

RUN mkdir /var/qpid
COPY qpidconfig/ /var/qpid/

# Add entrypoint
COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 5672 8080