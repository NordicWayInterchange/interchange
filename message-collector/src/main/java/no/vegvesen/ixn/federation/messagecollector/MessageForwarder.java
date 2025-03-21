package no.vegvesen.ixn.federation.messagecollector;

import jakarta.jms.*;
import no.vegvesen.ixn.ConnectionCreator;
import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageForwarder implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageForwarder.class);

    private final String writeUrl;
    private final String writeExchange;
    private final SSLContext writeContext;
    private final ConnectionCreator connectionCreator;
    private final String readUrl;
    private final String readSource;
    private final AtomicBoolean running;

    public MessageForwarder(
            String writeUrl,
            String writeExchange,
            SSLContext writeContext,
            ConnectionCreator connectionCreator,
            String readUrl,
            String readSource
    ) {
        this.writeUrl = writeUrl;
        this.writeExchange = writeExchange;
        this.writeContext = writeContext;
        this.connectionCreator = connectionCreator;
        this.readUrl = readUrl;
        this.readSource = readSource;
        this.running = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        running.set(true);
        //Broker "consumer"
        try (Source writeSource = new Source(writeUrl, writeExchange, writeContext)) {
            writeSource.start();
            logger.debug("Connected to write to destination {}, url {},", writeExchange, writeUrl);
            try (jakarta.jms.Connection readConnection = connectionCreator.createConnection(readUrl)) {
                try (Session session = readConnection.createSession(Session.AUTO_ACKNOWLEDGE)) {
                    Destination readDestination = session.createQueue(readSource);
                    try (MessageConsumer consumer = session.createConsumer(readDestination)) {
                        logger.info("Subscribed to url {}, destination {}", readUrl, readDestination);
                        while (running.get()) {
                            try {
                                Message message = consumer.receive(500); //Listener schedule, might be changed...
                                if (message != null) {
                                    MessageForwardUtil.send(writeSource.getProducer(), message);
                                    logger.trace("Message {} received from queue {}", message.getJMSMessageID(), readSource);
                                }
                            } catch (JMSException e) {
                                running.set(false);
                            }
                        }
                        logger.debug("Exiting collector thread");
                    }
                }
            } catch (JMSException e) {
                running.set(false);
                throw new RuntimeException(e);
            }
        } catch (NamingException | JMSException e) {
            running.set(false);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running.set(false);
    }
}
