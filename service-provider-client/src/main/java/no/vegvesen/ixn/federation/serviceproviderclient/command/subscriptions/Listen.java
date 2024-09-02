package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import static picocli.CommandLine.*;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "listen", description = "")
public class Listen implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Option(names = {"-s", "--selector"}, description = "")
    String selector;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        client.addSubscription(new AddSubscriptionsRequest(client.getUser(), Set.of(new AddSubscription(selector))));
        ListSubscriptionsResponse listSubscriptionsResponse = client.getServiceProviderSubscriptions();
        String subscriptionId = listSubscriptionsResponse.getSubscriptions().stream().findFirst().get().getId();

        GetSubscriptionResponse subscription = client.getSubscription(subscriptionId);
        while(!subscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)){
            subscription = client.getSubscription(subscriptionId);
            TimeUnit.SECONDS.sleep(2);
        }
        LocalEndpointApi endpointApi = client.getSubscription(subscription.getId()).getEndpoints().stream().findFirst().get();
        String url = "amqps://"+endpointApi.getHost();

        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", endpointApi.getHost(), url);
        AtomicInteger returnCode = new AtomicInteger(0);
        ExceptionListener exceptionListener = e -> {
            returnCode.compareAndSet(0, 1);
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                endpointApi.getSource(),
                parentCommand.getParent().createSSLContext(),
                new Sink.DefaultMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }
}

