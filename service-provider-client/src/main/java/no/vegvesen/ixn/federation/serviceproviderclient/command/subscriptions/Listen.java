package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import no.vegvesen.ixn.NewSink;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.apache.qpid.jms.JmsConnectionFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import javax.naming.Context;
import java.io.File;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Command(name = "listen", description = "Add subscription and receive messages")
public class Listen implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(multiplicity = "1")
    SubscriptionsOption option;

    @Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @Option(names = {"-c", "--comment"})
    String description;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        List<LocalActorSubscription> subscriptions;
        if(option.file != null){
            ObjectMapper mapper = new ObjectMapper();
            AddSubscriptionsRequest request = mapper.readValue(option.file, AddSubscriptionsRequest.class);
            AddSubscriptionsResponse addSubscriptionsResponse = client.addSubscription(request);
            subscriptions = addSubscriptionsResponse.getSubscriptions().stream().toList();
        }
        else if(option.selector != null){
            AddSubscriptionsResponse addSubscriptionsResponse = client.addSubscription(new AddSubscriptionsRequest(client.getUser(), Set.of(new AddSubscription(option.selector, description))));
            subscriptions = addSubscriptionsResponse.getSubscriptions().stream().toList();
        } else if (option.id != null) {
            GetSubscriptionResponse getSubscriptionResponse = client.getSubscription(option.id);
            subscriptions = List.of(new LocalActorSubscription(
                    getSubscriptionResponse.getId(),
                    getSubscriptionResponse.getPath(),
                    getSubscriptionResponse.getSelector(),
                    getSubscriptionResponse.getConsumerCommonName(),
                    getSubscriptionResponse.getLastUpdatedTimestamp(),
                    getSubscriptionResponse.getStatus(),
                    null,
                    getSubscriptionResponse.getDescription())
            );

        } else {
            throw  new RuntimeException("Need to specify either id, selector or file");
        }

        List<GetSubscriptionResponse> createdSubscriptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(subscriptions.size());
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            System.out.println(subscriptions.size() + " subscriptions created");
            for (LocalActorSubscription subscription : subscriptions) {

                executorService.submit(() -> {

                    String id = subscription.getId();
                    System.out.println("Checking subscription " + id);
                    GetSubscriptionResponse mySubscription = client.getSubscription(id);
                    while (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.REQUESTED)) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            mySubscription = client.getSubscription(id);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            latch.countDown();
                            throw new RuntimeException(e);
                        }
                    }

                    if (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)) {
                        if (mySubscription.getConsumerCommonName().equals(client.getUser())) {
                            //LocalSubscription, need to wait for the endpoints to be set
                            System.out.println("Redirect subscription " + mySubscription.getId() + " created, waiting for endpoints");
                            while (mySubscription.getEndpoints().isEmpty()) {
                                try {
                                    TimeUnit.SECONDS.sleep(2);
                                    mySubscription = client.getSubscription(id);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    latch.countDown();
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                        createdSubscriptions.add(mySubscription);
                    } else {
                        System.out.printf("Unexpected subscription status %s for subscription %s, skipping%n", mySubscription.getStatus(), mySubscription.getId());
                    }
                    latch.countDown();
                });
            }
        }
        latch.await();
        final CountDownLatch counter = new CountDownLatch(1);
        ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        NewSink sink = new NewSink(parentCommand.getParent().createSSLContext());
        ConnectionPool connectionPool = new ConnectionPool( url -> {
            try {
                Connection conn = sink.createConnection(url, exceptionListener);
                conn.start();
                return conn;
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
        Sink.DefaultMessageListener listener = directory != null ? new Sink.DefaultMessageListener(directory) : new Sink.DefaultMessageListener();
        for (GetSubscriptionResponse subscription : createdSubscriptions) {
            for (LocalEndpointApi endpoint : subscription.getEndpoints()) {
                String url = endpoint.toUrl();
                Connection connection = connectionPool.createConnection(url);
                Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(endpoint.getSource());
                MessageConsumer consumer = session.createConsumer(destination);
                consumer.setMessageListener(listener);
            }
        }
        counter.await();
        connectionPool.close();
        return 0;
    }

    private static class SubscriptionsOption {
        @Option(names = {"-f", "--filename"}, description = "The subscription json file")
        File file;

        @Option(names = {"-s", "--selector"}, description = "The subscriptions selector")
        String selector;

        @Option(names = {"-i", "--id"}, description = "The subscription Id")
        String id;
    }

    public static class ConnectionPool {

        private final ConnectionCreator connectionCreator;

        private ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
        public ConnectionPool(ConnectionCreator connectionCreator) {
            this.connectionCreator = connectionCreator;
        }

        public Connection createConnection(String url) {
            return connections.computeIfAbsent(url, connectionCreator::createConnection);
        }

        public void close() {
            connections.forEach( (s, c) -> {
                try {
                    c.close();
                } catch (JMSException e) {
                    System.out.println("Exception while closing connection: " + e);
                }
            });
        }

        public interface ConnectionCreator {
            Connection createConnection(String url);
        }

    }

}

