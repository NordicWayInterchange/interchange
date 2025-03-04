package no.vegvesen.ixn;

import jakarta.jms.*;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Testcontainers
public class MultipleListenersQpidTest extends QpidDockerBaseIT {

    private static final Logger logger = LoggerFactory.getLogger(MultipleListenersQpidTest.class);

    public static final String HOSTNAME = "localhost";

    private static final String SP_NAME = "king_gustaf";
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(MultipleListenersQpidTest.class),"my_ca", HOSTNAME, "routing_configurer", SP_NAME);


    @Container
    public final QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOSTNAME,
            HOSTNAME,
            Path.of("multiple-listeners")
    ).withLogConsumer(new Slf4jLogConsumer(logger));


    @Test
    public void listenToMultipleQueues() throws JMSException, NamingException, InterruptedException {
        System.out.println(qpidContainer.getHttpUrl());
        SSLContext context = sslClientContext(stores, SP_NAME);
        try (Source sender = new Source(qpidContainer.getAmqpsUrl(), "incomingExchange", context)) {
            sender.start();
            TextMessage message = sender.getSession().createTextMessage("This is my message");
            sender.getProducer().send(message,DeliveryMode.NON_PERSISTENT,Message.DEFAULT_PRIORITY,Message.DEFAULT_TIME_TO_LIVE);
            NewSink sink = new NewSink(context);
            try (Connection connection = sink.createConnection(qpidContainer.getAmqpsUrl())) {
                ListenerContainer container = new ListenerContainer();
                connection.setExceptionListener( e ->
                        {
                            try {
                                System.out.println("Exception handler");
                                container.stop();
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                );
                connection.start();
                for (String queueName : List.of("out-1", "out-2")) {
                    Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                    Destination destination = session.createQueue(queueName);
                    container.run(new Listener(session,destination));

                }
                TimeUnit.SECONDS.sleep(1);
                container.stop();
            }

        }

    }

    private static class ListenerContainer {
        private final ExecutorService executorService = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
        private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

        public void stop() throws InterruptedException {
            for (Listener l : listeners) {
                l.stop();
            }
            executorService.shutdown();
        }

        public void run(Listener listener) {
            executorService.execute(listener);
            listeners.add(listener);
        }
    }

    private static class Listener implements Runnable {
        private final Session session;
        private final Destination destination;
        private final AtomicBoolean running = new AtomicBoolean(true);

        public Listener(Session session, Destination destination) {
            this.session = session;
            this.destination = destination;
        }

        @Override
        public void run() {
            System.out.println("Starting listener");
            try (MessageConsumer consumer = session.createConsumer(destination)) {
                System.out.println("Consumer created");
                while (running.get()) {
                    try {
                        System.out.println("Waiting for message");
                        Message received = consumer.receive();
                        if (received == null) {
                            running.set(false);
                            System.out.println("Received null message");
                        } else {
                            System.out.println(received.getBody(String.class));
                        }
                    } catch (JMSException e) {
                        System.out.println("Exception caught");
                        running.set(false);
                    }
                }
                System.out.println("Done");

            } catch (JMSException e) {
                System.out.println("Could not create consumer");
                e.printStackTrace();
            }
        }

        public void stop() {
            running.set(false);
        }
    }
}
