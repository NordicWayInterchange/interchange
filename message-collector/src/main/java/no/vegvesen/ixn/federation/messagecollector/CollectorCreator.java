package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

import jakarta.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

@Component
@ConfigurationPropertiesScan(basePackages = "no.vegvesen.ixn")
public class CollectorCreator {


    private final SSLContext sslContext;
    private final Logger logger = LoggerFactory.getLogger(CollectorCreator.class);
    private final String localIxnDomainName;
    private final String localIxnFederationPort;

    CollectorCreator(SSLContext sslContext,
                     String localIxnDomainName,
                     String localIxnFederationPort) {
        this.sslContext = sslContext;
        this.localIxnDomainName = localIxnDomainName;
        this.localIxnFederationPort = localIxnFederationPort;
    }

    @Autowired
    public CollectorCreator(SslBundles sslBundles, CollectorProperties collectorProperties, InterchangeNodeProperties interchangeNodeProperties) {
        this.sslContext = sslBundles.getBundle("external-service").createSslContext();
        this.localIxnDomainName = interchangeNodeProperties.getName();
        this.localIxnFederationPort = collectorProperties.getLocalIxnFederationPort();
    }

    MessageCollectorListener setupCollection(ListenerEndpoint listenerEndpoint) {
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName, localIxnFederationPort);
        String localExchange = listenerEndpoint.getTarget();
        logger.debug("Write URL: {}, exchange {}", writeUrl, localExchange);
        Source writeSource = new Source(writeUrl, localExchange, sslContext);

        String readUrl = String.format("amqps://%s:%s", listenerEndpoint.getHost(), listenerEndpoint.getPort());
        String readQueue = listenerEndpoint.getSource();
        Sink readSink = new Sink(readUrl, readQueue, sslContext);
        logger.info("Fetching messages from URL {}, queue {} ; write to URL {} target {}", readUrl, readQueue, writeUrl, localExchange);

        MessageCollectorListener listener = new MessageCollectorListener(readSink, writeSource);
        try {
            writeSource.start();
            writeSource.setExceptionListener(listener);
        } catch (NamingException | JMSException e) {
            listener.teardown();
            throw new MessageCollectorException(String.format("Could not start source at URL '%s', exchange '%s', tearing down.",writeUrl,writeSource), e);
        }
        try {
            readSink.startWithMessageListener(listener);
            readSink.setExceptionListener(listener);

        } catch (NamingException | JMSException e) {
            listener.teardown();
            throw new MessageCollectorException(String.format("Could not start sink at URL '%s', source '%s', tearing down.",readUrl,readQueue), e);
        }
        return listener;
    }


}
