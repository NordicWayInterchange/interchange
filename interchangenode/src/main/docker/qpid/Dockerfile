FROM java:8

RUN cd /usr/local \
	&& wget http://apache.uib.no/qpid/broker-j/7.0.6/binaries/apache-qpid-broker-j-7.0.6-bin.tar.gz \
	&& tar -xvf apache-qpid-broker-j-7.0.6-bin.tar.gz

RUN mkdir /var/qpid
COPY qpidconfig/ /var/qpid/

# Add entrypoint
COPY docker-entrypoint.sh /
RUN chmod u+x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 5672 8080

# Check if qpid is reachable.
HEALTHCHECK --interval=3s --timeout=3s --retries=7 \
    CMD curl -I localhost:8080