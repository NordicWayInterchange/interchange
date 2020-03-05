package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.*;
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

    MessageCollectorListener setupCollection(Neighbour ixn) throws JMSException, NamingException {
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName, localIxnFederationPort);
        logger.debug("Write URL: {}, queue {}", writeUrl, writeQueue);
        IxnContext writeContext = new IxnContext(writeUrl, writeQueue,null);
        Destination writeDestination = writeContext.getSendQueue();
        Connection writeConnection = writeContext.createConnection(sslContext);
        Session writeSession = writeConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = writeSession.createProducer(writeDestination);

        String readUrl = ixn.getMessageChannelUrl();
        String readQueue = this.localIxnDomainName;
        IxnContext readContext = new IxnContext(readUrl,null, readQueue);
        logger.debug("Read URL: {}, queue {}",readUrl, readQueue);
        Destination readDestination = readContext.getReceiveQueue();
        Connection readConnection = readContext.createConnection(sslContext);
        Session readSession = readConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);

        MessageConsumer consumer = readSession.createConsumer(readDestination);
        MessageCollectorListener listener = new MessageCollectorListener(consumer,producer);
        consumer.setMessageListener(listener);

        writeConnection.setExceptionListener(listener);
        readConnection.setExceptionListener(listener);
        readConnection.start();
        writeConnection.start();
        return listener;

    }


}
