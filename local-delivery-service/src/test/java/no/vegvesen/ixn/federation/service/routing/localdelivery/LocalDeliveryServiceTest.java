package no.vegvesen.ixn.federation.service.routing.localdelivery;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.LocalDeliveryStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/*
TODO see if this should be a part of the IT test intead,
makes it much easier.
 */
@ExtendWith(MockitoExtension.class)
public class LocalDeliveryServiceTest {

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private OutgoingMatchRepository outgoingMatchRepository;

    @Mock
    private LocalDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new LocalDeliveryService(serviceProviderRepository, outgoingMatchRepository);
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

        when(outgoingMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus("our-node", 5671, serviceProvider);
        //The status for the delivery should not have changed
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
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
        when(outgoingMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus("our-node", 5671, serviceProvider);
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
        verify(outgoingMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());
    }
}
