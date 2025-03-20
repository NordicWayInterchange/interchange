package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import no.vegvesen.ixn.ExceptionListeningConnectionCreator;
import no.vegvesen.ixn.NewSink;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.SinkConnectionPool;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        Queue<GetSubscriptionResponse> results = new ArrayBlockingQueue<>(subscriptions.size());
        AtomicInteger numItemsLeft = new AtomicInteger(subscriptions.size());
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            System.out.println(subscriptions.size() + " subscriptions created");
            for (LocalActorSubscription subscription : subscriptions) {

                executorService.execute(
                        new WaitForSubscription(client, subscription, results)
                );
            }
        }
        final CountDownLatch counter = new CountDownLatch(1);
        final ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        NewSink sink = new NewSink(parentCommand.getParent().createSSLContext());
        SinkConnectionPool connectionPool = new SinkConnectionPool(new ExceptionListeningConnectionCreator(sink, exceptionListener));
        Sink.DefaultMessageListener listener = directory != null ? new Sink.DefaultMessageListener(directory) : new Sink.DefaultMessageListener();
        while (numItemsLeft.get() > 0) {
                GetSubscriptionResponse getSubscriptionResponse = results.poll();
                for (LocalEndpointApi endpoint : getSubscriptionResponse.getEndpoints()) {
                    String url = endpoint.toUrl();
                    Connection connection = connectionPool.createConnection(url);
                    Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                    Destination destination = session.createQueue(endpoint.getSource());
                    MessageConsumer consumer = session.createConsumer(destination);
                    consumer.setMessageListener(listener);
                }
                numItemsLeft.decrementAndGet();
        }
        System.out.println("All listeners started");
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


    private static class WaitForSubscription implements Runnable {
        private final LocalActorSubscription subscription;
        private final ServiceProviderClient client;
        private final Queue<GetSubscriptionResponse> workQueue;

        public WaitForSubscription(ServiceProviderClient client, LocalActorSubscription subscription,Queue<GetSubscriptionResponse> workQueue) {
            this.subscription = subscription;
            this.client = client;
            this.workQueue = workQueue;
        }

        @Override
        public void run() {

            String id = subscription.getId();
            System.out.println("Checking subscription " + id);
            GetSubscriptionResponse mySubscription = client.getSubscription(id);
            while (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.REQUESTED)) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    mySubscription = client.getSubscription(id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }

            if (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)) {
                if (mySubscription.getConsumerCommonName().equals(client.getUser())) {
                    //redirect subscription, need to wait for the endpoints to be set
                    System.out.println("Redirect subscription " + mySubscription.getId() + " created, waiting for endpoints");
                    while (mySubscription.getEndpoints().isEmpty()) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            mySubscription = client.getSubscription(id);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }

                }
            } else {
                throw new RuntimeException(String.format("Unexpected subscription status %s for subscription %s, skipping", mySubscription.getStatus(), mySubscription.getId()));
            }
            //Add the subscription to the blocking queue
            workQueue.add(mySubscription);

        }
    }

}

