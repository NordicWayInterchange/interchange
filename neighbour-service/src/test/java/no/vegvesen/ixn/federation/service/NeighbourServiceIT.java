package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
                new NeighbourSubscriptionRequest(Collections.emptySet()),
                new SubscriptionRequest(
                        Sets.newLinkedHashSet(
                                new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "interchangeA"))));
        Neighbour interchangeB = new Neighbour("interchangeB",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(
                        Sets.newLinkedHashSet(
                                new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.REQUESTED, "interchangeA"))),
                new SubscriptionRequest(
                        Sets.newLinkedHashSet(
                                new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "interchangeA"))));
        repository.save(interchangeA);
        repository.save(interchangeB);

        List<Neighbour> interchanges = service.listNeighboursToConsumeMessagesFrom();
        assertThat(interchanges).hasSize(1);
    }

    @Test
    public void incomingSubscriptionRequestReturnsPathForSubscriptionAndTimestamp() {
        Neighbour neighbour = new Neighbour("myNeighbour",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(Collections.emptySet()),
                new SubscriptionRequest(Collections.emptySet()));
        repository.save(neighbour);


        SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(
                new SubscriptionRequestApi("myNeighbour",
                        Collections.singleton(new RequestedSubscriptionApi("originatingCountry = 'NO'", "myNeighbour")))
        );
        Set<RequestedSubscriptionResponseApi> subscriptions = responseApi.getSubscriptions();
        assertThat(subscriptions).hasSize(1);
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

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(2);
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
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(2);

        NeighbourSubscription sub = repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptionById(Integer.parseInt(no.getId()));

        neighbour = repository.findByName(neighbour.getName());
        neighbour.getNeighbourRequestedSubscriptions().deleteSubscriptions(Collections.singleton(sub));
        service.saveNeighbour(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(1);
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
        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(1);

        neighbour = repository.findByName(neighbour.getName());
        NeighbourSubscription sub = neighbour.getNeighbourRequestedSubscriptions().getSubscriptionById(Integer.parseInt(no.getId()));

        neighbour.getNeighbourRequestedSubscriptions().deleteSubscriptions(Collections.singleton(sub));
        service.saveNeighbour(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(0);
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

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(2);

        RequestedSubscriptionApi sub3 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='FI'", "my-neighbour5");

        SubscriptionRequestApi subscriptionRequestApi2 = new SubscriptionRequestApi("my-neighbour5", new HashSet<>(Collections.singleton(sub3)));

        service.incomingSubscriptionRequest(subscriptionRequestApi2);

        service.saveSetupRouting(neighbour);

        assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(3);
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
        CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi(
                name,
                Collections.singleton(
                        new CapabilitySplitApi(
                            new DenmApplicationApi(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "1.0",
                                Collections.emptySet(),
                                Collections.singleton(1)
                            ),
                            new MetadataApi()
                ))
        );
        Set<CapabilitySplit> localCapabilities = Collections.emptySet();
        service.incomingCapabilities(capabilitiesApi, localCapabilities);
        assertThat(repository.findByName(name).getCapabilities().getCapabilities()).hasSize(1);
        //Now, try again, with the same capabilties, to simulate double post.
        service.incomingCapabilities(capabilitiesApi,localCapabilities);
        assertThat(repository.findByName(name).getCapabilities().getCapabilities()).hasSize(1);
    }

    @Test
    public void teardownSubscriptionAndNeighbourPostsIdenticalNewSubscription() {
        String selector = "a = 'hello'";
        String name = "teardown-and-new-neighbour-request";
        Neighbour neighbour = new Neighbour(
                name,
                new Capabilities(),
                new NeighbourSubscriptionRequest(
                    Collections.singleton(
                            new NeighbourSubscription(selector,NeighbourSubscriptionStatus.CREATED,name)
                    )
                ),
                new SubscriptionRequest()
        );
        repository.save(neighbour);

        SubscriptionRequestApi addRequest = new SubscriptionRequestApi(
                name,
                Collections.singleton(
                        new RequestedSubscriptionApi(
                                selector
                        )
                )
        );
        SubscriptionResponseApi subscriptionResponseApi = service.incomingSubscriptionRequest(addRequest);
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out,subscriptionResponseApi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void pollingASubscriptionWithStatusTearDown() {
        String name = "tear-down-neighbour";
        Neighbour neighbour = new Neighbour(
                name,
                new Capabilities(),
                new NeighbourSubscriptionRequest(
                        Collections.singleton(
                                new NeighbourSubscription("a = 'hello'",NeighbourSubscriptionStatus.TEAR_DOWN,name)
                        )
                ),
                new SubscriptionRequest()
        );
        Neighbour saved = repository.save(neighbour);
        NeighbourSubscription subscription = saved.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get();
        SubscriptionPollResponseApi response = service.incomingSubscriptionPoll(name, subscription.getId());
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatusApi.ERROR);

    }

}
