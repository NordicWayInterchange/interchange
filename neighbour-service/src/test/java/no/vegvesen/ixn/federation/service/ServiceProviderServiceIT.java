package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.Shard;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class ServiceProviderServiceIT {

    @Autowired
    ServiceProviderRepository repository;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Mock
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    ServiceProviderService service;

    @Autowired
    OutgoingMatchRepository outgoingMatchRepository;


    @Test
    public void repositoryIsAutowired() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void serviceIsAutowired() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void redirectEndpointsAreSavedFromNeighbour() {
        String serviceProviderName = "my-service-provider";
        String selector = "originatingCountry = 'NO'";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);

        serviceProvider.addLocalSubscription(localSubscription);

        repository.save(serviceProvider);

        Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, serviceProviderName);
        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);

        subscription.setEndpoints(Collections.singleton(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription, serviceProviderName);
        matchRepository.save(match);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).isNotEmpty();
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);
    }

    @Test
    public void redirectEndpointIsRemovedWhenSubscriptionToNeighbourIsRemoved() {
        String serviceProviderName = "my-service-provider";
        String selector = "originatingCountry = 'NO'";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);

        serviceProvider.addLocalSubscription(localSubscription);

        repository.save(serviceProvider);

        Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, serviceProviderName);
        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);

        subscription.setEndpoints(Collections.singleton(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription, serviceProviderName);
        matchRepository.save(match);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).isNotEmpty();
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);

        neighbourRepository.deleteAll();
        matchRepository.deleteAll();

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(0);
    }

    @Test
    public void localDeliveryGetsEndpointWithExchangeNameAsTarget(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint("host",5671, "target");
        delivery.setEndpoints(new HashSet<>(Arrays.asList(endpoint)));
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));

        repository.save(serviceProvider);
        service.updateNewLocalDeliveryEndpoints(serviceProvider.getName(), "host", 5671);
        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedAgainServiceProvider.getDeliveries().stream().findFirst().get().getEndpoints()).hasSize(1);

        savedAgainServiceProvider.getDeliveries().stream().findFirst().get().setExchangeName("exchangeName");
        repository.save(savedAgainServiceProvider);

        service.updateNewLocalDeliveryEndpoints(serviceProvider.getName(), "host", 5671);
        savedAgainServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedAgainServiceProvider.getDeliveries().stream().findFirst().get().getEndpoints()).hasSize(2);

    }

    @Test
    public void deliveryReceivesExchangeNameWhenItDoesNotExist(){

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.REQUESTED);
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));

        // Will only receive Exchange Name if outgoing match(es) exist
        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, null, serviceProvider.getName());
        outgoingMatchRepository.save(outgoingMatch);

        repository.save(serviceProvider);
        service.updateDeliveryStatus(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());

        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getExchangeName()).contains("del");
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExist(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.CREATED);
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        repository.save(serviceProvider);

        service.updateDeliveryStatus(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExistAndNoMatchingCapabilitiesExists(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.REQUESTED);
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));

        repository.save(serviceProvider);
        service.updateDeliveryStatus(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void doNotRemoveLocalDeliveryEndpointIfItHasOutGoingMatches(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);
        delivery.setExchangeName("target");
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint("host",5671, "target");
        delivery.setEndpoints(new HashSet<>(Arrays.asList(endpoint)));

        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, null, serviceProvider.getName());
        outgoingMatchRepository.save(outgoingMatch);
        repository.save(serviceProvider);

        service.updateTearDownLocalDeliveryEndpoints(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getEndpoints()).hasSize(1);

    }

    @Test
    public void removeLocalDeliveryEndpointIfItHasNoMatches(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);

        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint("host",5671, "target");
        delivery.setEndpoints(new HashSet<>(Arrays.asList(endpoint)));

        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, null, serviceProvider.getName());

        outgoingMatchRepository.save(outgoingMatch);
        repository.save(serviceProvider);

        service.updateTearDownLocalDeliveryEndpoints(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getEndpoints()).hasSize(0);

    }

    @Test
    public void capabilityIsNotRemovedWhenThereAreOutgoingMatches(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        Capabilities capabilities = new Capabilities();
        CapabilitySplit capabilitySplit = new CapabilitySplit();
        capabilitySplit.setStatus(CapabilityStatus.TEAR_DOWN);
        capabilities.setCapabilities(new HashSet<>(Arrays.asList(capabilitySplit)));

        OutgoingMatch outgoingMatch = new OutgoingMatch(null, capabilitySplit, serviceProvider.getName());
        outgoingMatchRepository.save(outgoingMatch);

        serviceProvider.setCapabilities(capabilities);
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(1);

    }

    @Test
    public void capabilityIsRemovedWhenThereAreNoOutgoingMatches(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        Capabilities capabilities = new Capabilities();
        CapabilitySplit capabilitySplit = new CapabilitySplit(null, new Metadata());
        capabilitySplit.setStatus(CapabilityStatus.TEAR_DOWN);
        capabilities.setCapabilities(new HashSet<>(Arrays.asList(capabilitySplit)));

        serviceProvider.setCapabilities(capabilities);
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(0);
    }

    @Test
    public void capabilityIsNotRemovedIfThereAreNoOutgoingMatchesButHasShards(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        Capabilities capabilities = new Capabilities();
        CapabilitySplit capabilitySplit = new CapabilitySplit();
        capabilitySplit.setStatus(CapabilityStatus.TEAR_DOWN);

        Metadata metadata = new Metadata();
        metadata.setShards(List.of(new Shard()));
        capabilitySplit.setMetadata(metadata);
        capabilities.setCapabilities(new HashSet<>(Arrays.asList(capabilitySplit)));

        serviceProvider.setCapabilities(capabilities);
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(1);
    }

    @Test
    public void DeliveryWithErrorGetsRemovedFromServiceProvider(){
        String serviceProviderName = "my-service-provider";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
        LocalDelivery delivery = new LocalDelivery();
        delivery.setStatus(LocalDeliveryStatus.ERROR);
        serviceProvider.addDeliveries(Set.of(delivery));

        repository.save(serviceProvider);
        service.removeTearDownIllegalAndErrorDeliveries(serviceProviderName);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getDeliveries()).hasSize(0);
    }

}
