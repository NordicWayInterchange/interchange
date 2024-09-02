package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import static picocli.CommandLine.*;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "listen", description = "")
public class Listen implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    SubscriptionsOption option;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        if(option.file != null){
            ObjectMapper mapper = new ObjectMapper();
            AddSubscriptionsRequest request = mapper.readValue(option.file, AddSubscriptionsRequest.class);
            client.addSubscription(request);
        }
        else{
            client.addSubscription(new AddSubscriptionsRequest(client.getUser(), Set.of(new AddSubscription(option.selector))));
        }

        ListSubscriptionsResponse listSubscriptionsResponse = client.getSubscriptions();
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

    private static class SubscriptionsOption {
        @Option(names = {"-f", "--filename"}, required = true, description = "The deliveries json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The delivery selector")
        String selector;
    }
}

