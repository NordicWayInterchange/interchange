package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine.*;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Command(name = "count", description = "Count number of messages until client is killed by Ctrl+C")
public class CountMessages implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(multiplicity = "1")
    SubscriptionsOption option;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        System.out.println("Counting messages until program is exited with ^C. Wait for server connection... ");

        AtomicLong counter = new AtomicLong();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    System.out.println(String.format("Received %d messages", counter.get()));
                }
        ));

        String id;
        if(option.subscriptionId != null){
            id = option.subscriptionId;
        }
        else{
            AddSubscriptionsRequest request = new AddSubscriptionsRequest(client.getUser(), Set.of(new AddSubscription(option.selector)));
            id = client.addSubscription(request).getSubscriptions().stream().filter(sub -> sub.getSelector().equals(option.selector)).findFirst().orElseThrow(
                    () -> new RuntimeException("Server indicated subscription was added, but could not find it in response")
            ).getId();
        }

        GetSubscriptionResponse subscription = client.getSubscription(id);
        while (subscription.getStatus().equals(LocalActorSubscriptionStatusApi.REQUESTED)) {
            subscription = client.getSubscription(subscription.getId());
            TimeUnit.SECONDS.sleep(2);
        }

        if (! subscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected subscription status %s for subscription %s",subscription.getStatus(),subscription.getId()));

        }

        LocalEndpointApi endpointApi = client
                .getSubscription(subscription.getId())
                .getEndpoints()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Could not determine endpoint for subscription with id %s",id)));
        String url = "amqps://"+endpointApi.getHost();

        CountDownLatch latch = new CountDownLatch(1);
        try (Sink sink = new Sink(url, endpointApi.getSource(), parentCommand.getParent().createSSLContext(), message -> counter.incrementAndGet())) {
            sink.start();
            latch.await();
        }
        return 0;
    }
    private static class SubscriptionsOption{
        @Option(names = {"-i", "--id"})
        String subscriptionId;

        @Option(names = {"-s", "--selector"})
        String selector;

    }
}
