package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    NeighbourRepository neighbourRepository;

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Test
    void oneServiceProviderOneLocalSubscription() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        doReturn(Lists.list(new ServiceProvider(
                1,
                "sp-1",
                new Capabilities(),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        ))).when(serviceProviderRepository).findAll();
        doReturn(Lists.list(new Neighbour(
                "node-A",
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest()
        ))).when(neighbourRepository).findAll();
        String name = "local-node";

        serviceProviderService.updateLocalSubscriptions(name, "5671");

        verify(serviceProviderRepository).findAll();
        verify(neighbourRepository).findAll();

        //TODO service providers are saved each time, no matter if it's changed or not
        verify(serviceProviderRepository,times(1)).save(any());

    }

    @Test
    public void testUpdateServiceProviderSubscriptionWithLocalBrokerUrlAndConsumerCommonNameSameAsServiceProviderName() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        String neighbourName = "node-A";
        String selector = "a = b";
        int subscriptionId = 1;
        String localNodeName = "local-node";
        List<Neighbour> neighbours = Arrays.asList(new Neighbour(
                neighbourName,
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest(
                       SubscriptionRequestStatus.ESTABLISHED, //TODO does this have anything to say??
                       Collections.singleton(new Subscription(
                               subscriptionId,
                               SubscriptionStatus.CREATED,
                               selector,
                               "local-node/subscriptions/1",
                               localNodeName,
                               Collections.singleton(new Endpoint(
                                       neighbourName,
                                       localNodeName,
                                       5671
                               ))
                       ))
                )
        ));
        String serviceProviderName = "sp-1";
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                selector,
                LocalDateTime.now(),
                new HashSet<>());
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(localSubscription),
                new HashSet<>(),
                LocalDateTime.now()
        );
        serviceProviderService.updateServiceProviderSubscriptionsWithHostAndPort(neighbours,serviceProvider,localNodeName, "5671");
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
        LocalSubscription subscription = serviceProvider.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getLocalEndpoints())
                .hasSize(1)
                .allMatch(b -> localNodeName.equals(b.getHost()))
                .allMatch(b -> 5671 == b.getPort())
                ; //.allMatch(b -> serviceProviderName.equals(b.getSource())); TODO local queue names
    }

    @Test
    public void testUpdateServiceProviderSubscriptionWithLocalBrokerUrlWithConsumerCommonNameSameAsIxnName() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        String neighbourName = "node-A";
        String selector = "a = b";
        String queueName = "my-queue";
        int subscriptionId = 1;
        String localNodeName = "local-node";
        String serviceProviderName = "sp-1";
        List<Neighbour> neighbours = Arrays.asList(new Neighbour(
                neighbourName,
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest(
                        SubscriptionRequestStatus.ESTABLISHED, //TODO does this have anything to say??
                        Collections.singleton(new Subscription(
                                subscriptionId,
                                SubscriptionStatus.CREATED,
                                selector,
                                "local-node/subscriptions/1",
                                null, //QueueconsumerUser is most often null from other node.
                                Collections.singleton(new Endpoint(
                                        localNodeName,
                                        "messages.node-A.eu",
                                        5671
                                ))
                        ))
                )
        ));
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                selector,
                LocalDateTime.now(),
                new HashSet<>());
        localSubscription.setQueueName(queueName);
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(localSubscription),
                new HashSet<>(),
                LocalDateTime.now()
        );
        final String localMessageHost = "messages.local-node";
        final String localMessagePort = "5671";
        serviceProviderService.updateServiceProviderSubscriptionsWithHostAndPort(neighbours,serviceProvider, localMessageHost, localMessagePort);
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
        LocalSubscription subscription = serviceProvider.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getLocalEndpoints())
                .hasSize(1)
                .allMatch(b -> "messages.local-node".equals(b.getHost()))
                .allMatch(b -> 5671 == b.getPort())
                .allMatch(b -> queueName.equals(b.getSource()));
    }

    @Test
    public void updateServiceProviderDeliveryWithOnramp() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        String selector = "originatingCountry = 'NO' AND protocolVersion = 'DENM:1.1.0' AND quadTree like '%,123%,' AND causeCode like '%,1,%'";
        String localNodeName = "local-node";
        String serviceProviderName = "sp-1";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDateTime.now(),
                LocalDeliveryStatus.REQUESTED
        );
        DenmCapability capability = new DenmCapability(
                "publisher-1",
                "NO",
                "DENM:1.1.0",
                Collections.singleton("123"),
                Collections.singleton("1")
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(
                        Capabilities.CapabilitiesStatus.KNOWN,
                        Collections.singleton(capability)),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        );
        serviceProvider.addDeliveries(Collections.singleton(localDelivery));
        doReturn(Arrays.asList(serviceProvider)).when(serviceProviderRepository).findAll();
        serviceProviderService.updateLocalDeliveries(localNodeName, "5671");
        assertThat(serviceProvider.getDeliveries()).hasSize(1);
        LocalDelivery delivery = serviceProvider.getDeliveries().stream().findFirst().get();
        assertThat(delivery.getEndpoints())
                .hasSize(1)
                .allMatch(b -> b.getHost().equals(localNodeName))
                .allMatch(b -> b.getPort() == 5671)
                .allMatch(b -> b.getSelector().equals(selector))
                .allMatch(b -> b.getTarget().equals("onramp"));
        verify(serviceProviderRepository,times(1)).findAll();
        verify(serviceProviderRepository).save(any());
    }
}
