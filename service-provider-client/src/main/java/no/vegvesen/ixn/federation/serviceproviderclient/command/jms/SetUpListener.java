package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@CommandLine.Command(name = "listen", description = "")
public class SetUpListener implements Callable<Integer> {

    @CommandLine.ParentCommand
    MessagesCommand parentCommand;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        AddCapabilitiesRequest capabilitiesRequest = new AddCapabilitiesRequest("test", Set.of(new CapabilityApi(
                new DatexApplicationApi("String publisherId", "String publicationId", "String originatingCountry", "String protocolVersion", List.of("1"), "String publicationType", "String publisherName"),
                new MetadataApi()
        )));
        client.addCapability(capabilitiesRequest);

        AddDeliveriesRequest deliveriesRequest = new AddDeliveriesRequest("test", Set.of(new SelectorApi("originatingCountry='NO'")));
        client.addServiceProviderDeliveries(deliveriesRequest);

        AddSubscriptionsRequest subscriptionsRequest = new AddSubscriptionsRequest("test", Set.of(new AddSubscription("originatingCountry='NO'")));
        client.addSubscription(subscriptionsRequest);
        ListSubscriptionsResponse listSubscriptionsResponse = client.getServiceProviderSubscriptions();
        LocalActorSubscription subscription = listSubscriptionsResponse.getSubscriptions().stream().findFirst().get();

        while(!client.getSubscription(subscription.getId()).getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)){
            TimeUnit.SECONDS.sleep(2);
        }
        LocalEndpointApi endpointApi = client.getSubscription(subscription.getId()).getEndpoints().stream().findFirst().get();
        String url = "amqps://"+endpointApi.getHost();
        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", endpointApi.getHost(), parentCommand.getUrl());
        AtomicInteger returnCode = new AtomicInteger(0);
        ExceptionListener exceptionListener = e -> {
            returnCode.compareAndSet(0, 1);
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                endpointApi.getSource(),
                parentCommand.createContext(),
                new Sink.DefaultMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }
}
