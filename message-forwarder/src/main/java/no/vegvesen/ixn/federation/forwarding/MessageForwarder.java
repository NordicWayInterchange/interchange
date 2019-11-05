package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.*;


@Service
public class MessageForwarder {

    private NeighbourFetcher neighbourFetcher;
    private ForwarderProperties properties;
    private final SSLContext sslContext;
    private Map<String,MessageForwardListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageForwarder.class);


    @Autowired
    public MessageForwarder(NeighbourFetcher fetcher, SSLContext sslContext, ForwarderProperties properties) {
        this.neighbourFetcher = fetcher;
        this.properties = properties;
        this.listeners = new HashMap<>();
        this.sslContext = sslContext;
    }

    @Scheduled(fixedRate = 30000)
    public void runSchedule() throws NamingException, JMSException {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }


    private void checkListenerList() {
        Set<String> remoteNames = listeners.keySet();
        for (String remoteName : remoteNames) {
            MessageForwardListener listener = listeners.get(remoteName);
            if (! listener.isRunning()) {
                listeners.remove(remoteName);
                logger.info("Removed stopped listener {}", remoteName);
            }
        }

    }

    public void setupConnectionsToNewNeighbours() throws NamingException, JMSException {
        List<Neighbour> interchanges = neighbourFetcher.listNeighbourCandidates();
        List<String> interchangeNames = new ArrayList<>();
        for (Neighbour ixn : interchanges) {
            String name = ixn.getName();
            interchangeNames.add(name);
            if (! listeners.containsKey(name)) {
                try {
                    logger.info("Setting up ixn with name {}, port {}", ixn.getName(), ixn.getMessageChannelPort());
                    MessageProducer producer = createProducerToRemote(ixn);

                    IxnContext context = createContext(ixn);
                    Connection connection = createConnection(context);
                    MessageConsumer messageConsumer = createDestination(context, connection);

                    MessageForwardListener messageListener = new MessageForwardListener(messageConsumer, producer);
                    messageConsumer.setMessageListener(messageListener);
                    connection.setExceptionListener(messageListener);

                    listeners.put(name, messageListener);
                } catch (JMSException e) {
                    logger.warn("Tried to create connection to {}, but failed with exception.",name,e);
                }
            } else {
                if (listeners.get(name).isRunning()) {
                    logger.debug("Listener for {} is still running with no changes", name);
                } else {
                    logger.debug("Non-running listener detected, name {}",name);
                }
            }
        }
        for (String ixnName : listeners.keySet()) {
            if (! interchangeNames.contains(ixnName)) {
                logger.info("Listener for {} is now being removed",ixnName);
                MessageForwardListener toRemove = listeners.remove(ixnName);
                toRemove.teardown();
            }
        }
    }

    public IxnContext createContext(Neighbour ixn) throws NamingException {
        String readUrl = String.format("amqps://%s:%s",properties.getLocalIxnDomainName(),properties.getLocalIxnFederationPort());
        String readQueue = ixn.getName();
        logger.debug("Creating destination for messages on queue [{}] from [{}]", readQueue, readUrl);
        return new IxnContext(readUrl, null, readQueue);
    }

    public Connection createConnection(IxnContext context) throws NamingException, JMSException {
        Connection connection = context.createConnection(sslContext);
        connection.start();
        return connection;
    }

    public MessageConsumer createDestination(IxnContext context, Connection connection) throws JMSException, NamingException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queueR = context.getReceiveQueue();
        return session.createConsumer(queueR);
    }

    public MessageProducer createProducerToRemote(Neighbour ixn) throws NamingException, JMSException {
        logger.info("Connecting to {}", ixn.getName());
        String writeUrl = ixn.getMessageChannelUrl();
        logger.debug("Creating producer on url [{}]", writeUrl);
        IxnContext writeContext = new IxnContext(writeUrl, properties.getRemoteWritequeue(), null);

        Destination queueS = writeContext.getSendQueue();
        Connection writeConnection = writeContext.createConnection(sslContext);
        writeConnection.start();
        Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return writeSession.createProducer(queueS);
    }

}
