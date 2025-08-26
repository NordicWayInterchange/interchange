package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Mock
    OutgoingMatchRepository outgoingMatchRepository;

    @Mock
    MatchRepository matchRepository;

    ServiceProviderService service;

    @BeforeEach
    void setUp() {
        service = new ServiceProviderService(serviceProviderRepository, outgoingMatchRepository, matchRepository);
    }

    @Test
    public void updateDeliveryStatusShouldNotChangeTheStatusOfADeliveryThatDoesNotHaveAnExchangeYet() {
        LocalDelivery localDelivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                "publicationId = '0001:0001'",
                LocalDeliveryStatus.REQUESTED
        );
        Capability capability = new Capability(
                1,
                new DenmApplication(
                        "0001",
                        "0001:0001",
                        "NO",
                        "1.0",
                        List.of("0122"),
                        List.of(6)
                ),
                new Metadata()
        );
        capability.getMetadata().setShardCount(1);
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                new Capabilities(
                        Collections.singleton(
                                capability
                        )
                ),
                Collections.emptySet(),
                Collections.singleton(
                        localDelivery
                ),
                LocalDateTime.now()

        );

        when(serviceProviderRepository.findByName(serviceProvider.getName())).thenReturn(serviceProvider);
        when(outgoingMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);
        //The status for the delivery should not have changed
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(outgoingMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());

    }


    @Test
    public void updateDeliveryStatusShouldMakeDeliveryNoOverlapIfNoCapabilitiesMatch() {
        LocalDelivery localDelivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                "publicationId = '0001:0001'",
                LocalDeliveryStatus.REQUESTED
        );

        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                new Capabilities(),
                Collections.emptySet(),
                Collections.singleton(
                        localDelivery
                ),
                LocalDateTime.now()

        );
        when(serviceProviderRepository.findByName(serviceProvider.getName())).thenReturn(serviceProvider);
        when(outgoingMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus(serviceProvider.getName(), "our-node", 5671);
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(outgoingMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());
    }
}
