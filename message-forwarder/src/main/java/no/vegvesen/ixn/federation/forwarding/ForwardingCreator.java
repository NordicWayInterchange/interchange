package no.vegvesen.ixn.federation.forwarding;

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

    MessageForwardListener setupForwarding(Neighbour ixn) throws JMSException, NamingException {
        MessageProducer producer = createProducerToRemote(ixn);
        IxnContext context = createContext(ixn);
        Connection connection = createConnection(context);
        MessageConsumer messageConsumer = createDestination(context, connection);
        MessageForwardListener messageListener = new MessageForwardListener(messageConsumer, producer);
        messageConsumer.setMessageListener(messageListener);
        connection.setExceptionListener(messageListener);
        return messageListener;
    }

    MessageProducer createProducerToRemote(Neighbour ixn) throws NamingException, JMSException {
        String writeUrl = ixn.getMessageChannelUrl();
        logger.debug("Creating producer for neighbour [{}] on url [{}] and destination [{}]", ixn.getName(), writeUrl, properties.getRemoteWritequeue());
        IxnContext writeContext = new IxnContext(writeUrl, properties.getRemoteWritequeue(), null);

        Destination queueS = writeContext.getSendQueue();
        Connection writeConnection = writeContext.createConnection(sslContext);
        writeConnection.start();
        Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return writeSession.createProducer(queueS);
    }

    private IxnContext createContext(Neighbour ixn) throws NamingException {
        String readUrl = String.format("amqps://%s:%s",properties.getLocalIxnDomainName(),properties.getLocalIxnFederationPort());
        String readQueue = ixn.getName();
        logger.debug("Creating destination for messages on queue [{}] from [{}]", readQueue, readUrl);
        return new IxnContext(readUrl, null, readQueue);
    }

    private Connection createConnection(IxnContext context) throws NamingException, JMSException {
        Connection connection = context.createConnection(sslContext);
        connection.start();
        return connection;
    }

    private MessageConsumer createDestination(IxnContext context, Connection connection) throws JMSException, NamingException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queueR = context.getReceiveQueue();
        return session.createConsumer(queueR);
    }


}
