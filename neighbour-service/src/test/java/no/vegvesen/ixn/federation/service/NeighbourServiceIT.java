package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourServiceIT {

    @Autowired
    private NeighbourRepository repository;

    @Autowired
    private NeighbourService service;

    @Test
    public void serviceIsAutowired() {
        assertThat(service).isNotNull();
    }

    @Test
    public void testFetchingNeighbourWithCorrectStatus() {
        Neighbour interchangeA = new Neighbour("interchangeA",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,
                        Sets.newLinkedHashSet(
                                new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "interchangeA"))));
        Neighbour interchangeB = new Neighbour("interchangeB",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED,
                        Sets.newLinkedHashSet(
                                new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.REQUESTED, "interchangeA"))),
                new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED,
                        Sets.newLinkedHashSet(
                                new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "interchangeA"))));
        repository.save(interchangeA);
        repository.save(interchangeB);

        List<Neighbour> interchanges = service.listNeighboursToConsumeMessagesFrom();
        assertThat(interchanges).size().isEqualTo(1);
    }

    @Test
    public void incomingSubscriptionRequestReturnsPathForSubscriptionAndTimestamp() {
        Neighbour neighbour = new Neighbour("myNeighbour",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.EMPTY, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
        repository.save(neighbour);


        SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(
                new SubscriptionRequestApi("myNeighbour",
                        Collections.singleton(new RequestedSubscriptionApi("originatingCountry = 'NO'", "myNeighbour")))
        );
        Set<RequestedSubscriptionResponseApi> subscriptions = responseApi.getSubscriptions();
        assertThat(subscriptions.size()).isEqualTo(1);
        RequestedSubscriptionResponseApi subscriptionApi = subscriptions.stream().findFirst().get();
        assertThat(subscriptionApi.getPath()).isNotNull();
        assertThat(subscriptionApi.getLastUpdatedTimestamp()).isGreaterThan(0);
    }

    @Test
    public void emptyIncomingSubscriptionRequestReturnsException() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("my-neighbour1");
        repository.save(neighbour);

        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour1", Collections.emptySet());

        assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() ->
                service.incomingSubscriptionRequest(subscriptionRequestApi));
    }

    @Test
    public void incomingSubscriptionRequestIsSavedWithSubscriptionRequestStatusEstablished() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("my-neighbour2");
        repository.save(neighbour);

        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'", "my-neighbour2");
        RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'", "my-neighbour2");

        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour2", new HashSet<>(Arrays.asList(sub1, sub2)));

        service.incomingSubscriptionRequest(subscriptionRequestApi);

        assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
        service.saveSetupRouting(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(NeighbourSubscriptionRequestStatus.ESTABLISHED);
    }

    @Test
    public void deleteOneSubscriptionAndGetSubscriptionRequestStatusModified() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("my-neighbour3");
        repository.save(neighbour);

        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'", "my-neighbour3");
        RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'", "my-neighbour3");

        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour3", new HashSet<>(Arrays.asList(sub1, sub2)));

        SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(subscriptionRequestApi);
        RequestedSubscriptionResponseApi no = responseApi.getSubscriptions().stream().filter(r -> r.getSelector().contains("NO")).findFirst().get();

        assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
        service.saveSetupRouting(neighbour);
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(NeighbourSubscriptionRequestStatus.ESTABLISHED);

        NeighbourSubscription sub = repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptionById(Integer.parseInt(no.getId()));

        service.saveDeleteSubscriptions("my-neighbour3", Collections.singleton(sub));

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(NeighbourSubscriptionRequestStatus.MODIFIED);
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
    }

    @Test
    public void deleteLastSubscriptionTearsDownSubscriptionRequest() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("my-neighbour4");
        repository.save(neighbour);

        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'", "my-neighbour4");

        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour4", new HashSet<>(Collections.singleton(sub1)));

        SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(subscriptionRequestApi);
        RequestedSubscriptionResponseApi no = responseApi.getSubscriptions().stream().filter(r -> r.getSelector().contains("NO")).findFirst().get();

        assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
        service.saveSetupRouting(neighbour);
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(NeighbourSubscriptionRequestStatus.ESTABLISHED);

        NeighbourSubscription sub = repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptionById(Integer.parseInt(no.getId()));

        service.saveDeleteSubscriptions("my-neighbour4", Collections.singleton(sub));

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(NeighbourSubscriptionRequestStatus.EMPTY);
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(0);
    }

    @Test
    public void incomingSubscriptionsAreAddedToAlreadyExistingSubscriptions() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("my-neighbour5");
        repository.save(neighbour);

        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'", "my-neighbour5");
        RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'", "my-neighbour5");


        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour5", new HashSet<>(Arrays.asList(sub1, sub2)));

        service.incomingSubscriptionRequest(subscriptionRequestApi);

        assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
        service.saveSetupRouting(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(2);

        RequestedSubscriptionApi sub3 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='FI'", "my-neighbour5");

        SubscriptionRequestApi subscriptionRequestApi2 = new SubscriptionRequestApi("my-neighbour5", new HashSet<>(Collections.singleton(sub3)));

        service.incomingSubscriptionRequest(subscriptionRequestApi2);

        service.saveSetupRouting(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(3);
    }

    @Test
    public void incomingSubscriptionWithConsumerCommonNameSameAsServiceProviderName() {
        Neighbour neighbour = new Neighbour();
        String neighbourName = "my-service-provider-wants-direct-subscription";
        neighbour.setName(neighbourName);

        repository.save(neighbour);

        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi(neighbourName,new HashSet<>(Arrays.asList(
                new RequestedSubscriptionApi("messageType='DATEX2' AND originatingCountry = 'NO'","service-provider")
        )));

        service.incomingSubscriptionRequest(subscriptionRequestApi);

        Neighbour persistedNeighbour = repository.findByName(neighbourName);
        assertThat(persistedNeighbour.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(1);
        NeighbourSubscription subscription = persistedNeighbour.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getConsumerCommonName().equals("service-provider")).isTrue();
    }


    @Test
    public void incomingCapabilitiesSeveralTimesWithSameDataShouldResultInTheSameSet() {
        Neighbour neighbour = new Neighbour();
        String name = "neighbour-with-incoming-capabilities-twice";
        neighbour.setName(name);
        repository.save(neighbour);
        CapabilitiesApi capabilitiesApi = new CapabilitiesApi(
                name,
                Collections.singleton(
                        new DenmCapabilityApi(
                                "NO-123",
                                "NO",
                                "1.0",
                                Collections.emptySet(),
                                Collections.singleton("1")
                        )
                )
        );
        Set<Capability> localCapabilities = Collections.emptySet();
        service.incomingCapabilities(capabilitiesApi, localCapabilities);
        assertThat(repository.findByName(name).getCapabilities().getCapabilities()).hasSize(1);
        //Now, try again, with the same capabilties, to simulate double post.
        service.incomingCapabilities(capabilitiesApi,localCapabilities);
        assertThat(repository.findByName(name).getCapabilities().getCapabilities()).hasSize(1);
    }

}
