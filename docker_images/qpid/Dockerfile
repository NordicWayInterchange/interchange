FROM java:8

RUN cd /usr/local \
	&& wget http://apache.uib.no/qpid/broker-j/7.0.6/binaries/apache-qpid-broker-j-7.0.6-bin.tar.gz \
	&& tar -xvf apache-qpid-broker-j-7.0.6-bin.tar.gz

#Copy the broker config file. there is certainly a better way like jakob did with qdrouterd
#COPY init-config.json /usr/local

# Add entrypoint
COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 5672 8080