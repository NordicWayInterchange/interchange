package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class MessageForwarder {


    public static class MessageForwarderException extends RuntimeException {
        public MessageForwarderException(Exception e) {
            super(e);
        }

        public MessageForwarderException(String cause) {
            super(cause);
        }
    }

    public static class MessageForwardListener implements MessageListener {
        private AtomicBoolean running;
        private final MessageConsumer messageConsumer;
        private final MessageProducer producer;

        public MessageForwardListener(MessageConsumer messageConsumer, MessageProducer producer) {
            this.messageConsumer = messageConsumer;
            this.producer = producer;
            this.running = new AtomicBoolean(true);
        }

        @Override
        public void onMessage(Message message) {
            if (running.get()) {
                try {
                    producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                } catch (JMSException e) {
                    //TODO what to do? Probably need to mark as unusable, and tear down???
                    try {
                        producer.close();
                        running.set(false);
                    } catch (JMSException e1) {
                        e1.printStackTrace();
                    }
                    throw new MessageForwarderException(e);
                }
            } else {
                throw new MessageForwarderException("Not running!");
            }
        }



    }

    @Value("${forwarder.localIxnDomainName}")
    private String localIxnDomainName;

    @Value("${forwarder.localIxnFederationPort}")
    private String localIxnFederationPort;

    private NeighbourFetcher neighbourFetcher;
    private final SSLContext sslContext;
    //TODO this will probably not work in a threaded environment...
    //private List<Interchange> connectedInterchanges;
    private Map<String,MessageForwardListener> listeners;
    //private List<String> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageForwarder.class);


    @Autowired
    public MessageForwarder(NeighbourFetcher fetcher, SSLContext sslContext) {
        this.neighbourFetcher = fetcher;
        //this.connectedInterchanges = new ArrayList<>();
        this.listeners = new HashMap<>();
        this.sslContext = sslContext;
    }

    @Scheduled(fixedRate = 60000)
    public void runSchedule() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, NamingException, JMSException, IOException {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }


    private void checkListenerList() {
        Set<String> remoteNames = listeners.keySet();
        for (String remoteName : remoteNames) {
            MessageForwardListener listener = listeners.get(remoteName);
            if (! listener.running.get()) {
                listeners.remove(remoteName);
            }
        }

    }

    public void setupConnectionsToNewNeighbours() throws NamingException, JMSException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        List<Interchange> interchanges = neighbourFetcher.listNeighbourCandidates();
        for (Interchange ixn : interchanges) {
            String name = ixn.getName();
            //if (! connectedInterchanges.contains(ixn)) {
            if (! listeners.containsKey(name)) {
                System.out.println(String.format("name: %s, address %s:%s, fedIn: %s status: %s",ixn.getName(),ixn.getDomainName(),ixn.getControlChannelPort(),ixn.getFedIn(),ixn.getInterchangeStatus()));
                logger.debug("Found nex Ixn %s, setting up connections");
                MessageProducer producer = createProducerToRemote(ixn);
                MessageConsumer messageConsumer = createConsumerFromLocal(ixn);
                MessageForwardListener messageListener = new MessageForwardListener(messageConsumer, producer);
                messageConsumer.setMessageListener(messageListener);
                
                //TODO Should we have a single object that is a message consumer? Or one per destination? messageConsumer.setMessageListener(this);
                //connectedInterchanges.add(ixn);
                listeners.put(name,messageListener);
            } else {
                logger.debug("No new Ixn found");
            }
        }
    }

    public MessageConsumer createConsumerFromLocal(Interchange ixn) throws NamingException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException, JMSException {
        String readUrl = String.format("amqps://%s:%s",localIxnDomainName,localIxnFederationPort);
        String readQueue = ixn.getName();
        Hashtable<Object, Object> env = createReadContext(readUrl, readQueue);

        Context context = new javax.naming.InitialContext(env);
        JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
        factory.setPopulateJMSXUserID(true);
        factory.setSslContext(sslContext);
        Destination queueR = (Destination) context.lookup("receiveQueue");

        //TODO need to set key and trust managers on this
        Connection connection = factory.createConnection("local", "password");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session.createConsumer(queueR);
    }

    public MessageProducer createProducerToRemote(Interchange ixn) throws NamingException, JMSException {
        System.out.println(String.format("Connecting to %s",ixn.getDomainName()));
        //First, create a connection for the output, then the input,
        //TODO for now, assume the read queue name is the name of the interchange we're connecting
        //the write queue to.
        //remote queue:
        //amqp://<ixn.getDomainName()>:<ixn.getControlChannelPort>, queue name "fedEx"
        String writeUrl = String.format("amqps://%s:%s",ixn.getDomainName(),ixn.getControlChannelPort());
        String writeQueue = "fedEx";
        Hashtable<Object, Object> writeEnv = createWriteContext(writeUrl, writeQueue);


        Context writeContext = new javax.naming.InitialContext(writeEnv);
        JmsConnectionFactory writeFactory = (JmsConnectionFactory) writeContext.lookup("myFactoryLookupTLS");
        writeFactory.setPopulateJMSXUserID(true);
        writeFactory.setSslContext(sslContext);

        Destination queueS = (Destination) writeContext.lookup("sendQueue");
        Connection writeConnection = writeFactory.createConnection("remote", "password");
        //writeConnection.
        writeConnection.start();
        Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return writeSession.createProducer(queueS);
    }

    private Hashtable<Object, Object> createReadContext(String readUrl, String readQueue) {
        Hashtable<Object, Object> env = createContext();
        env.put("connectionfactory.myFactoryLookupTLS", readUrl);
        env.put("queue.receiveQueue", readQueue);
        return env;
    }

    private Hashtable<Object, Object> createWriteContext(String writeUrl, String writeQueue) {
        Hashtable<Object, Object> env = createContext();
        env.put("connectionfactory.myFactoryLookupTLS", writeUrl);
        env.put("queue.sendQueue", writeQueue);
        return env;
    }


    private Hashtable<Object,Object> createContext() {
        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        return  env;
    }




}
