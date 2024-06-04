package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.SubscriptionDeleteException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.SubscriptionMatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;



@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourSubscriptionDeleteServiceIT {

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    SubscriptionMatchRepository subscriptionMatchRepository;

    @Autowired
    ListenerEndpointRepository listenerEndpointRepository;

    @Autowired
    NeighbourSubscriptionDeleteService service;

    @MockBean
    NeighbourFacade mockNeighbourFacade;

    @Test
    public void serviceIsAutowired() {
        assertThat(service).isNotNull();
    }

    //Get back 200 - OK
    @Test
    public void subscriptionIsDeleted() {
        String neighbourName = "my-neighbour";
        Neighbour neighbour = new Neighbour();
        neighbour.setName(neighbourName);

        Subscription ourSubscription = new Subscription("messageType = 'DENM' and originatingCountry = 'NO'", SubscriptionStatus.TEAR_DOWN);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(ourSubscription);

        neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(subscriptions));
        neighbourRepository.save(neighbour);

        service.deleteSubscriptions(mockNeighbourFacade);

        assertThat(neighbourRepository.findByName(neighbourName).getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
    }

    //Get back 404 - NOT FOUND
    @Test
    public void subscriptionDeleteWithSubscriptionNotFound() {
        String neighbourName = "my-neighbour";
        Neighbour neighbour = new Neighbour();
        neighbour.setName(neighbourName);

        Subscription ourSubscription = new Subscription("messageType = 'DENM' and originatingCountry = 'NO'", SubscriptionStatus.TEAR_DOWN);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(ourSubscription);

        neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(subscriptions));
        neighbourRepository.save(neighbour);

        doThrow(new SubscriptionNotFoundException("", new RuntimeException())).when(mockNeighbourFacade).deleteSubscription(any(), any());
        service.deleteSubscriptions(mockNeighbourFacade);

        assertThat(neighbourRepository.findByName(neighbourName).getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
    }

    //Get back 4xx - something else gone wrong
    @Test
    public void subscriptionDeleteWithSubscriptionGoneWrong() {
        String neighbourName = "my-neighbour";
        Neighbour neighbour = new Neighbour();
        neighbour.setName(neighbourName);

        Subscription ourSubscription = new Subscription("messageType = 'DENM' and originatingCountry = 'NO'", SubscriptionStatus.TEAR_DOWN);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(ourSubscription);

        neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(subscriptions));
        neighbourRepository.save(neighbour);

        doThrow(new SubscriptionDeleteException("", new RuntimeException())).when(mockNeighbourFacade).deleteSubscription(any(), any());
        service.deleteSubscriptions(mockNeighbourFacade);

        assertThat(neighbourRepository.findByName(neighbourName).getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
    }

    @Test
    public void subscriptionIsNotDeletedBeforeSubscriptionShardIsRemoved() {
        String neighbourName = "my-neighbour";
        Neighbour neighbour = new Neighbour();
        neighbour.setName(neighbourName);

        Subscription ourSubscription = new Subscription("messageType = 'DENM' and originatingCountry = 'NO'", SubscriptionStatus.TEAR_DOWN);

        Endpoint endpoint = new Endpoint("my-source", "my-host", 5671);
        endpoint.setShard(new SubscriptionShard("my-exchange"));

        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.add(endpoint);
        ourSubscription.setEndpoints(endpoints);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(ourSubscription);

        neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(subscriptions));
        neighbourRepository.save(neighbour);

        service.deleteSubscriptions(mockNeighbourFacade);

        assertThat(neighbourRepository.findByName(neighbourName).getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
    }

    @Test
    public void subscriptionIsDeletedAfterSubscriptionShardIsRemoved() {
        String neighbourName = "my-neighbour";
        Neighbour neighbour = new Neighbour();
        neighbour.setName(neighbourName);

        Subscription ourSubscription = new Subscription("messageType = 'DENM' and originatingCountry = 'NO'", SubscriptionStatus.TEAR_DOWN);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(ourSubscription);

        neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(subscriptions));
        neighbourRepository.save(neighbour);

        service.deleteSubscriptions(mockNeighbourFacade);

        assertThat(neighbourRepository.findByName(neighbourName).getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
    }

}
