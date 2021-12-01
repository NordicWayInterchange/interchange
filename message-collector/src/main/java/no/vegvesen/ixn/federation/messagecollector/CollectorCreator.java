package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

@Component
@ConfigurationPropertiesScan(basePackages = "no.vegvesen.ixn")
public class CollectorCreator {


    private final SSLContext sslContext;
    private Logger logger = LoggerFactory.getLogger(CollectorCreator.class);
    private String localIxnDomainName;
    private String localIxnFederationPort;
    private String writeQueue;

    CollectorCreator(SSLContext sslContext,
                     String localIxnDomainName,
                     String localIxnFederationPort,
                     String writequeue) {
        this.sslContext = sslContext;
        this.localIxnDomainName = localIxnDomainName;
        this.localIxnFederationPort = localIxnFederationPort;
        this.writeQueue = writequeue;
    }

    @Autowired
    public CollectorCreator(SSLContext sslContext, CollectorProperties collectorProperties, InterchangeNodeProperties interchangeNodeProperties) {
        this.sslContext = sslContext;
        this.localIxnDomainName = interchangeNodeProperties.getName();
        this.localIxnFederationPort = collectorProperties.getLocalIxnFederationPort();
        this.writeQueue = collectorProperties.getWritequeue();
    }

    MessageCollectorListener setupCollection(ListenerEndpoint listenerEndpoint) {
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName, localIxnFederationPort);
        logger.debug("Write URL: {}, queue {}", writeUrl, writeQueue);
        Source writeSource = new Source(writeUrl, writeQueue, sslContext);

        String readUrl = String.format("amqps://%s:%s", listenerEndpoint.getHost(), listenerEndpoint.getPort());
        String readQueue = listenerEndpoint.getSource();
        Sink readSink = new Sink(readUrl, readQueue, sslContext);
        logger.info("Fetching messages from {}, write to {}",readUrl,writeUrl);

        MessageCollectorListener listener = new MessageCollectorListener(readSink, writeSource);
        try {
            writeSource.start();
            writeSource.setExceptionListener(listener);
            readSink.startWithMessageListener(listener);
            readSink.setExceptionListener(listener);
        } catch (NamingException | JMSException e) {
            listener.teardown();
            throw new MessageCollectorException("Tried to set up a new MessageCollectorListener, tearing down.", e);
        }
        return listener;
    }


}
