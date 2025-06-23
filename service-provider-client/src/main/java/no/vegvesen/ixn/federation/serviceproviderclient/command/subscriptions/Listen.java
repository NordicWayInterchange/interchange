package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import no.vegvesen.ixn.ExceptionListeningConnectionCreator;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.PoolingConnectionCreator;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Command(name = "listen", description = "Add subscription and receive messages",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples:\n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" \n
                        serviceproviderclient subscriptions listen -i 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 \n
                        serviceproviderclient subscriptions listen -f sub.json \n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" -d directory \n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" -d directory -c "NO subscription" \n
                        # -d and -c is optional
                        """
        })
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
            AddSubscriptionsResponse addSubscriptionsResponse = client.addSubscription(new AddSubscriptionsRequest(client.getUser(), List.of(new AddSubscription(option.selector, description))));
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

        final CountDownLatch counter = new CountDownLatch(1);
        final ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        SSLContext sslContext = parentCommand.getParent().createSSLContext();
        PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(new ExceptionListeningConnectionCreator(sslContext, exceptionListener));
        //TODO need to handle errors!!!!
        System.out.println(subscriptions.size() + " subscriptions created");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (LocalActorSubscription subscription : subscriptions) {
            String subscriptionId = subscription.getId();
            System.out.println("Wait for subscription " + subscriptionId);
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(
                    new WaitForEndpoints(subscriptionId, client)
            ).thenApply(response -> {
                System.out.println("Listening for subscription " + subscriptionId);
                for (LocalEndpointApi endpoint : response.getEndpoints()) {
                    String url = endpoint.toUrl();
                    try (Connection connection = connectionPool.createConnection(url)) {
                        Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                        Destination destination = session.createQueue(endpoint.getSource());
                        MessageConsumer consumer = session.createConsumer(destination);
                        consumer.setMessageListener(directory != null ? new Sink.DefaultMessageListener(directory) : new Sink.DefaultMessageListener());
                        counter.await();
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
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


    private static class WaitForEndpoints implements Supplier<GetSubscriptionResponse> {
        private final String subscriptionId;
        private final ServiceProviderClient client;

        public WaitForEndpoints(String subscriptionId, ServiceProviderClient client) {
            this.subscriptionId = subscriptionId;
            this.client = client;
        }

        @Override
        public GetSubscriptionResponse get() {
            System.out.println("Checking subscription " + subscriptionId);
            GetSubscriptionResponse mySubscription = client.getSubscription(subscriptionId);
            while (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.REQUESTED)) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    mySubscription = client.getSubscription(subscriptionId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }

            if (mySubscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)) {
                if (mySubscription.getConsumerCommonName().equals(client.getUser())) {
                    //redirect subscription, need to wait for the endpoints to be set
                    System.out.println("Redirect subscription " + mySubscription.getId() + " created, waiting for endpoints");
                    int numtries = 0;
                    while (mySubscription.getEndpoints().isEmpty()) {
                        try {
                            if (numtries == 5) {
                                throw new RuntimeException(String.format("Could not get subscription %s in %d tries", subscriptionId, numtries));
                            }
                            numtries++;
                            TimeUnit.SECONDS.sleep(2);
                            mySubscription = client.getSubscription(subscriptionId);
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
            //workQueue.add(mySubscription);
            return mySubscription;

        }
    }
}

