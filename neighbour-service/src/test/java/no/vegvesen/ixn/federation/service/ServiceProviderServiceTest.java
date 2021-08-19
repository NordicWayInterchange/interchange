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
        Self self = new Self("local-node");

        serviceProviderService.updateLocalSubscriptions(self);

        verify(serviceProviderRepository).findAll();
        verify(neighbourRepository).findAll();

        //TODO service providers are saved each time, no matter if it's changed or not
        verify(serviceProviderRepository,times(1)).save(any());

    }

    @Test
    public void testUpdateServiceProviderSubscriptionWithLocalBrokerUrlCreateNewQueue() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        String neighbourName = "node-A";
        String selector = "a = b";
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
                               true,
                               serviceProviderName,
                               Collections.singleton(new Broker(
                                       serviceProviderName,
                                       "amqps://messages.node-A.eu"
                               ))
                       ))
                )
        ));
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                selector,
                LocalDateTime.now(),
                true,
                serviceProviderName,
                new HashSet<>());
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(localSubscription),
                new HashSet<>(),
                LocalDateTime.now()
        );
        serviceProviderService.updateServiceProviderSubscriptionsWithBrokerUrl(neighbours,serviceProvider,"amqps://messages.local-node");
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
        LocalSubscription subscription = serviceProvider.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getLocalBrokers())
                .hasSize(1)
                .allMatch(b -> "amqps://messages.node-A.eu".equals(b.getMessageBrokerUrl()))
                .allMatch(b -> serviceProviderName.equals(b.getQueueName()));
    }

    @Test
    public void testUpdateServiceProviderSubscriptionWithLocalBrokerUrlNonCreateNewQueue() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(neighbourRepository,serviceProviderRepository);
        String neighbourName = "node-A";
        String selector = "a = b";
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
                                false,
                                null, //QueueconsumerUser is most often null from other node.
                                Collections.singleton(new Broker(
                                        localNodeName,
                                        "amqps://messages.node-A.eu"
                                ))
                        ))
                )
        ));
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                selector,
                LocalDateTime.now(),
                false,
                serviceProviderName,
                new HashSet<>());
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(localSubscription),
                new HashSet<>(),
                LocalDateTime.now()
        );
        final String localMessageBrokerUrl = "amqps://messages.local-node";
        serviceProviderService.updateServiceProviderSubscriptionsWithBrokerUrl(neighbours,serviceProvider, localMessageBrokerUrl);
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
        LocalSubscription subscription = serviceProvider.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getLocalBrokers())
                .hasSize(1)
                .allMatch(b -> localMessageBrokerUrl.equals(b.getMessageBrokerUrl()))
                .allMatch(b -> serviceProviderName.equals(b.getQueueName()));
    }
}
