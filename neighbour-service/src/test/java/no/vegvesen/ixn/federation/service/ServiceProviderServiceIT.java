package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class ServiceProviderServiceIT extends PostgresContainerBase {

    @Autowired
    ServiceProviderRepository repository;

    @Autowired
    NeighbourRepository neighbourRepository;

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

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);

        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName,Set.of(localSubscription));

        repository.save(serviceProvider);

        Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, serviceProviderName);
        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);

        subscription.setEndpoints(Collections.singleton(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription);
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
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);
        ServiceProvider serviceProvider = new ServiceProvider(
                serviceProviderName,
                Set.of(localSubscription));
        repository.save(serviceProvider);

        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);
        Subscription subscription = new Subscription(
                SubscriptionStatus.CREATED,
                selector,
                "/",
                serviceProviderName,
                Set.of(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription);
        matchRepository.save(match);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).isNotEmpty();
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);

        matchRepository.deleteAll();
        neighbourRepository.deleteAll();

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(0);
    }


    @Test
    public void deliveryReceivesExchangeNameWhenItDoesNotExist(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));

        // Will only receive Exchange Name if outgoing match(es) exist
        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, null, serviceProvider.getName());
        outgoingMatchRepository.save(outgoingMatch);

        repository.save(serviceProvider);
        service.updateDeliveryStatus(serviceProvider.getName(), "my-interchange", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());

        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getEndpoints()).hasSize(1);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExist(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'",  LocalDeliveryStatus.CREATED, "Description");
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        repository.save(serviceProvider);

        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExistAndNoMatchingCapabilitiesExists(){
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", "Test subscription");
        Set<LocalDelivery> deliveries = Set.of(delivery);
        ServiceProvider serviceProvider = new ServiceProvider(
                "service-provider",
                new Capabilities(),
                Set.of(),
                deliveries,
                LocalDateTime.now()

        );

        repository.save(serviceProvider);
        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }


    @Test
    public void capabilityIsNotRemovedWhenThereAreOutgoingMatches(){
        String name = "service-provider";

        Capability capability = new Capability();
        capability.setStatus(CapabilityStatus.TEAR_DOWN);
        Capabilities capabilities = new Capabilities(Set.of(capability));


        OutgoingMatch outgoingMatch = new OutgoingMatch(null, capability, name);
        outgoingMatchRepository.save(outgoingMatch);

        ServiceProvider serviceProvider = new ServiceProvider(
                name,
                capabilities
        );
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(1);
    }

    @Test
    public void multipleCapabilitiesAreRemoved(){
        Capabilities capabilities = new Capabilities( Set.of(
                new Capability(
                        new DatexApplication(1+"test", 1+"test", 1+"test", 1+"test", List.of("123123"),"12", "pubname"),
                        new Metadata()
                ),
                new Capability(
                        new DatexApplication(2+"test", 2+"test", 2+"test", 2+"test", List.of("123123"),"123", "pubname"),
                        new Metadata()
                ),
                new Capability(
                        new DatexApplication(3+"test", 3+"test", 3+"test", 3+"test", List.of("123123"),"1234", "pubname"),
                        new Metadata()
                )));

        for(Capability i : capabilities.getCapabilities()){
            i.setStatus(CapabilityStatus.TEAR_DOWN);
        }
        ServiceProvider sp = new ServiceProvider("sp", capabilities);
        repository.save(sp);
        service.removeTearDownCapabilities(sp.getName());
        ServiceProvider savedServiceProvider = repository.findByName(sp.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities().size()).isEqualTo(0);
    }
    @Test
    public void capabilityIsRemovedWhenThereAreNoOutgoingMatches(){

        Capability capability = new Capability(null, new Metadata());
        capability.setStatus(CapabilityStatus.TEAR_DOWN);
        Capabilities capabilities = new Capabilities(Set.of(capability));

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",capabilities);
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(0);
    }

    @Test
    public void capabilityIsNotRemovedIfThereAreNoOutgoingMatchesButHasShards(){


        Capability capability = new Capability(
                UUID.randomUUID().toString(),
                new DenmApplication(),
                new Metadata(),
                List.of(new CapabilityShard())
        );
        capability.setStatus(CapabilityStatus.TEAR_DOWN);
        Capabilities capabilities = new Capabilities(Set.of(capability));

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",capabilities);
        repository.save(serviceProvider);
        service.removeTearDownCapabilities(serviceProvider.getName());

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getCapabilities().getCapabilities()).hasSize(1);
    }

    @Test
    public void deliveryWithErrorGetsRemovedFromServiceProvider(){
        String serviceProviderName = "my-service-provider";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'", LocalDeliveryStatus.ERROR, "description");
        serviceProvider.addDeliveries(Set.of(delivery));

        repository.save(serviceProvider);
        service.removeTearDownIllegalAndErrorDeliveries(serviceProviderName);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getDeliveries()).hasSize(0);
    }

}
