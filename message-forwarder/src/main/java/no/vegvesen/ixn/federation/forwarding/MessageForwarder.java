package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


@Service
public class MessageForwarder {

    public static class MessageForwardListener implements MessageListener {

        private final MessageProducer producer;

        public MessageForwardListener(MessageProducer producer) {

            this.producer = producer;
        }

        @Override
        public void onMessage(Message message) {
            try {
                producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            } catch (JMSException e) {
                //TODO what to do? Probably need to mark as unusable, and tear down???
                e.printStackTrace();
            }
        }
    }


    private NeighbourFetcher neighbourFetcher;

    private String localIxnDomainName = "localhost";
    private String localIxnFederationPort = "6171";

    //Need some private list of connections.. TODO this will probably not work in a threaded environment...
    private List<Interchange> connectedInterchanges;
    private Logger logger = LoggerFactory.getLogger(MessageForwarder.class);


    @Autowired
    public MessageForwarder(NeighbourFetcher fetcher) {
        this.neighbourFetcher = fetcher;
        this.connectedInterchanges = new ArrayList<>();
    }

    //Call the rest api and get the list of queues
    //Or, use the database to get the neighbours, then check if I already have a connection
    //This should be scheduled(?)
    public void setupConnectionsToNewNeighbours() throws NamingException, JMSException {
        List<Interchange> interchanges = neighbourFetcher.listNeighbourCandidates();
        for (Interchange ixn : interchanges) {
            System.out.println(String.format("name: %s, address %s:%s, fedIn: %s status: %s",ixn.getName(),ixn.getDomainName(),ixn.getControlChannelPort(),ixn.getFedIn(),ixn.getInterchangeStatus()));
            if (! connectedInterchanges.contains(ixn)) {
                System.out.println(String.format("Connecting to %s",ixn.getDomainName()));
                //First, create a connection for the output, then the input,
                //TODO for now, assume the read queue name is the name of the interchange we're connecting
                //the write queue to.

                //remote queue:
                //amqp://<ixn.getDomainName()>:<ixn.getControlChannelPort>, queue name "fedEx"
                String writeUrl = String.format("amqp://%s:%s",ixn.getDomainName(),ixn.getControlChannelPort());
                String writeQueue = "fedEx";
                Hashtable<Object, Object> writeEnv = createWriteContext(writeUrl, writeQueue);


                Context writeContext = new javax.naming.InitialContext(writeEnv);
                JmsConnectionFactory writeFactory = (JmsConnectionFactory) writeContext.lookup("myFactoryLookupTLS");
                writeFactory.setPopulateJMSXUserID(true);
                Destination queueS = (Destination) writeContext.lookup("sendQueue");
                Connection writeConnection = writeFactory.createConnection("king_harald", "password");
                writeConnection.start();
                Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = writeSession.createProducer(queueS);

                //local connection:
                //"amqp://<local-hostname>:<local-control-channel-port>, queue name "<interchange-name<"
                String readUrl = String.format("amqps://%s:%s",localIxnDomainName,localIxnFederationPort);
                String readQueue = ixn.getName();
                Hashtable<Object, Object> env = createReadContext(readUrl, readQueue);

                Context context = new javax.naming.InitialContext(env);
                JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
                factory.setPopulateJMSXUserID(true);
                Destination queueR = (Destination) context.lookup("receiveQueue");

                //TODO need to set key and trust managers on this
                Connection connection = factory.createConnection("king_harald", "password");
                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer messageConsumer = session.createConsumer(queueR);


                messageConsumer.setMessageListener(new MessageForwardListener(producer));
                
                //TODO Should we have a single object that is a message consumer? Or one per destination? messageConsumer.setMessageListener(this);
                connectedInterchanges.add(ixn);
            }
        }
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
