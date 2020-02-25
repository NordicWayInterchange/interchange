package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

@Component
public class ForwardingCreator {


    private final ForwarderProperties properties;
    private final SSLContext sslContext;
    private Logger logger = LoggerFactory.getLogger(ForwardingCreator.class);

    @Autowired
    public ForwardingCreator(ForwarderProperties properties, SSLContext sslContext) {
        this.properties = properties;
        this.sslContext = sslContext;
    }

    //TODO logging!
    MessageForwardListener setupCollection(Neighbour ixn) throws JMSException, NamingException {
        String localIxnDomainName = properties.getLocalIxnDomainName();
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName,properties.getLocalIxnFederationPort());
        String writeQueue = "fedEx"; //TODO externalize
        logger.debug("Write URL: {}, queue {}", writeUrl,writeQueue);
        IxnContext writeContext = new IxnContext(writeUrl,writeQueue,null);
        Destination writeDestination = writeContext.getSendQueue();
        Connection writeConnection = writeContext.createConnection(sslContext);
        Session writeSession = writeConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = writeSession.createProducer(writeDestination);

        String readUrl = ixn.getMessageChannelUrl();
        IxnContext readContext = new IxnContext(readUrl,null, localIxnDomainName);
        logger.debug("Read URL: {}, queue {}",readUrl,localIxnDomainName);
        Destination readDestination = readContext.getReceiveQueue();
        Connection readConnection = readContext.createConnection(sslContext);
        Session readSession = readConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);

        MessageConsumer consumer = readSession.createConsumer(readDestination);
        MessageForwardListener listener = new MessageForwardListener(consumer,producer);
        consumer.setMessageListener(listener);

        writeConnection.setExceptionListener(listener);
        readConnection.setExceptionListener(listener);
        readConnection.start();
        writeConnection.start();
        return listener;

    }


}
