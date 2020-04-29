package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

@Component
public class CollectorCreator {


    private final SSLContext sslContext;
    private Logger logger = LoggerFactory.getLogger(CollectorCreator.class);
    private String localIxnDomainName;
    private String localIxnFederationPort;
    private String writeQueue;

    @Autowired
    public CollectorCreator(SSLContext sslContext,
                            @Value("${collector.localIxnDomainName}") String localIxnDomainName,
                            @Value("${collector.localIxnFederationPort}") String localIxnFederationPort,
                            @Value("${collector.writequeue}") String writequeue) {
        this.sslContext = sslContext;
        this.localIxnDomainName = localIxnDomainName;
        this.localIxnFederationPort = localIxnFederationPort;
        this.writeQueue = writequeue;
    }

    MessageCollectorListener setupCollection(Neighbour ixn) {
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName, localIxnFederationPort);
        logger.debug("Write URL: {}, queue {}", writeUrl, writeQueue);
        Source writeSource = new Source(writeUrl, writeQueue, sslContext);

        String readUrl = ixn.getMessageChannelUrl();
        String readQueue = this.localIxnDomainName;
		Sink readSink = new Sink(readUrl, readQueue, sslContext);

        MessageCollectorListener listener = new MessageCollectorListener(readSink, writeSource);
		try {
			writeSource.start();
			writeSource.setExceptionListener(listener);
			readSink.start(listener);
			readSink.setExceptionListener(listener);
		} catch (NamingException | JMSException e) {
			listener.teardown();
			throw new MessageCollectorException("Tried to set up a new MessageCollectorListener, tearing down.", e);
		}
        return listener;
    }


}
