package no.vegvesen.ixn.federation.service.routing.localdelivery;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.LocalDeliveryStatus;
import no.vegvesen.ixn.federation.model.OutgoingMatch;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LocalDeliveryServiceIT extends PostgresContainerBase {

    @Autowired
    private OutgoingMatchRepository outgoingMatchRepository;

    @Autowired
    private ServiceProviderRepository repository;

    @Autowired
    private LocalDeliveryService service;

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
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'",  LocalDeliveryStatus.CREATED, "Description", false);
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        repository.save(serviceProvider);

        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExistAndNoMatchingCapabilitiesExists(){
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        LocalDelivery delivery = new LocalDelivery();
        serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));

        repository.save(serviceProvider);
        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().get().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryWithErrorGetsRemovedFromServiceProvider(){
        String serviceProviderName = "my-service-provider";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'", LocalDeliveryStatus.ERROR, "description", false);
        serviceProvider.addDeliveries(Set.of(delivery));

        repository.save(serviceProvider);
        service.removeTearDownIllegalAndErrorDeliveries(serviceProviderName);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getDeliveries()).hasSize(0);
    }

}
