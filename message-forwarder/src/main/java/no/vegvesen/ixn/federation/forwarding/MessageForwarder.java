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

    //TODO This is the interface to use in order to set the listener running flag to false
    ExceptionListener exceptionListener = (e) -> e.printStackTrace();

    private NeighbourFetcher neighbourFetcher;
    private ForwarderProperties properties;
    private final SSLContext sslContext;
    //TODO this will probably not work in a threaded environment...
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
            }
        }

    }

    public void setupConnectionsToNewNeighbours() throws NamingException, JMSException {
        List<Neighbour> interchanges = neighbourFetcher.listNeighbourCandidates();
        for (Neighbour ixn : interchanges) {
            String name = ixn.getName();
            if (! listeners.containsKey(name)) {
                logger.debug("Found ixn with name {}, port {}",ixn.getName(),ixn.getMessageChannelPort());
                //System.out.println(String.format("name: %s, address %s:%s, fedIn: %s status: %s",ixn.getName(),ixn.getName(),ixn.getMessageChannelPort(),ixn.getFedIn(),ixn.getSubscriptionRequest().getStatus()));
                //logger.debug("Found nex Ixn %s, setting up connections");
                MessageProducer producer = createProducerToRemote(ixn);
                MessageConsumer messageConsumer = createConsumerFromLocal(ixn);
                MessageForwardListener messageListener = new MessageForwardListener(messageConsumer, producer);
                messageConsumer.setMessageListener(messageListener);
                
                //TODO Should we have a single object that is a message consumer? Or one per destination? messageConsumer.setMessageListener(this);
                listeners.put(name,messageListener);
            } else {
                logger.debug("No new Ixn found");
            }
        }
    }

    public MessageConsumer createConsumerFromLocal(Neighbour ixn) throws NamingException, JMSException {
        String readUrl = String.format("amqps://%s:%s",properties.getLocalIxnDomainName(),properties.getLocalIxnFederationPort());
        String readQueue = ixn.getName();

        IxnContext context = new IxnContext(readUrl, null, readQueue);
        Destination queueR = context.getReceiveQueue();

        Connection connection = context.createConnection(sslContext);
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session.createConsumer(queueR);
    }

    public MessageProducer createProducerToRemote(Neighbour ixn) throws NamingException, JMSException {
        System.out.println(String.format("Connecting to %s",ixn.getName()));
        String writeUrl = ixn.getMessageChannelUrl();
        IxnContext writeContext = new IxnContext(writeUrl, properties.getRemoteWritequeue(), null);

        Destination queueS = writeContext.getSendQueue();
        Connection writeConnection = writeContext.createConnection(sslContext);
        writeConnection.setExceptionListener(exceptionListener);
        writeConnection.start();
        Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return writeSession.createProducer(queueS);
    }

}
