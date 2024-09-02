package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import static picocli.CommandLine.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "listen", description = "")
public class Listen implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Option(names = {"-f", "--file"}, description = "")
    File file;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        AddCapabilitiesRequest capability;
        String consumerCommonName;
        if(file != null){
            ObjectMapper mapper = new ObjectMapper();
            capability = mapper.readValue(file, AddCapabilitiesRequest.class);
            consumerCommonName = capability.getName();
        }
        else{
            capability = getCapabilitiesRequest();
            consumerCommonName = "king_olav.bouvetinterchange.eu";
        }

        client.addCapability(capability);
        String selector = generateSelector(capability);

        AddDeliveriesRequest deliveriesRequest = new AddDeliveriesRequest(consumerCommonName, Set.of(new SelectorApi(selector)));
        client.addServiceProviderDeliveries(deliveriesRequest);

        AddSubscriptionsRequest subscriptionsRequest = new AddSubscriptionsRequest(consumerCommonName, Set.of(new AddSubscription(selector)));
        client.addSubscription(subscriptionsRequest);

        ListSubscriptionsResponse listSubscriptionsResponse = client.getServiceProviderSubscriptions();
        LocalActorSubscription subscription = listSubscriptionsResponse.getSubscriptions().stream().findFirst().get();

        while(!client.getSubscription(subscription.getId()).getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)){
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

    public AddCapabilitiesRequest getCapabilitiesRequest() {
        AddCapabilitiesRequest capabilitiesRequest = new AddCapabilitiesRequest();
        String publisherId = "bouvet";
        String originatingCountry="NO";
        List<String> quadTree = List.of("12");
        List<String> messageTypes = List.of("DATEX2", "DENM", "CAM", "SSEM", "SREM", "IVIM", "MAPEM", "SPATEM");
        for(int i = 0; i < 8; i++){
            CapabilityApi capability = new CapabilityApi();
            if(messageTypes.get(i).equals("DATEX2")){
                capability.setApplication(
                        new DatexApplicationApi(publisherId, publisherId + ":" +i, originatingCountry, messageTypes.get(i)+":1", quadTree, "warning", "bouvet")
                );
            }
            else if(messageTypes.get(i).equals("DENM")){
                capability.setApplication(
                        new DenmApplicationApi(publisherId, publisherId + ":" + i, originatingCountry, messageTypes.get(i)+":1", quadTree, List.of(6))
                );
            }
            else{
                capability.setApplication(
                        new ApplicationApi(messageTypes.get(i), publisherId, publisherId+":"+i, originatingCountry, messageTypes.get(i)+":1", quadTree));

            }
            capability.setMetadata(new MetadataApi());
            capabilitiesRequest.getCapabilities().add(capability);
        }
        return capabilitiesRequest;
    }

    public String generateSelector(AddCapabilitiesRequest capability) {
        StringBuilder sb = new StringBuilder();
        Iterator<CapabilityApi> capabilityApiIterator = capability.getCapabilities().iterator();
        while(capabilityApiIterator.hasNext()){
            CapabilityApi capabilityApi = capabilityApiIterator.next();
            sb.append("(publicationId='").append(capabilityApi.getApplication().getPublicationId()).append("') ");
            if(capabilityApiIterator.hasNext()){
                sb.append("OR ");
            }
        }
        return sb.toString();
    }
}

