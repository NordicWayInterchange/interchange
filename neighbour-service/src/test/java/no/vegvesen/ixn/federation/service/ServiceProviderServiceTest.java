package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.SubscriptionMatchRepository;
import no.vegvesen.ixn.federation.repository.DeliveryCapabilityMatchRepository;
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
    DeliveryCapabilityMatchRepository deliveryCapabilityMatchRepository;

    @Mock
    SubscriptionMatchRepository subscriptionMatchRepository;

    ServiceProviderService service;

    @BeforeEach
    void setUp() {
        service = new ServiceProviderService(serviceProviderRepository, deliveryCapabilityMatchRepository, subscriptionMatchRepository);
    }

    @Test
    public void updateDeliveryEndpointsAddsNewEndpoints() {
        ServiceProvider sp = new ServiceProvider("sp");
        String selector = "originatingCountry = 'NO'";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDeliveryStatus.CREATED
        );
        localDelivery.setExchangeName("my-exchange");
        sp.addDeliveries(Collections.singleton(localDelivery));

        Capability capability = new Capability(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("123"),
                        List.of(1)
                ),
                new Metadata()
        );
        capability.setStatus(CapabilityStatus.CREATED);
        sp.getCapabilities().addDataType(capability);

        when(serviceProviderRepository.findByName(any())).thenReturn(sp);
        service.updateNewLocalDeliveryEndpoints(sp.getName(), "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(1);
    }

    @Test
    public void onlyOneEndpointWhenThereIsMoreMatchesWithCapabilities() {
        ServiceProvider sp = new ServiceProvider("sp");
        String selector = "originatingCountry = 'NO'";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDeliveryStatus.CREATED
        );
        localDelivery.setExchangeName("my-exchange");
        sp.addDeliveries(Collections.singleton(localDelivery));

        Capability capability = new Capability(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("123"),
                        List.of(1)
                ),
                new Metadata()
        );
        capability.setStatus(CapabilityStatus.CREATED);
        sp.getCapabilities().addDataType(capability);

        Capability capability1 = new Capability(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("122"),
                        List.of(1)
                ),
                new Metadata()
        );
        capability1.setStatus(CapabilityStatus.CREATED);
        sp.getCapabilities().addDataType(capability1);

        when(serviceProviderRepository.findByName(any())).thenReturn(sp);
        service.updateNewLocalDeliveryEndpoints(sp.getName(), "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(1);
    }

    @Test
    public void noEndpointAddedWhenThereIsNoExchangePresentAfterMatchWithCapability() {
        ServiceProvider sp = new ServiceProvider("sp");
        String selector = "originatingCountry = 'NO'";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDeliveryStatus.CREATED
        );
        sp.addDeliveries(Collections.singleton(localDelivery));

        when(serviceProviderRepository.findByName(any())).thenReturn(sp);
        service.updateNewLocalDeliveryEndpoints(sp.getName(), "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(0);
    }


    @Test
    public void updateDeliveryStatusShouldNotChangeTheStatusOfADeliveryThatDoesNotHaveAnExchangeYet() {
        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "/deliveries/1",
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
        when(deliveryCapabilityMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus(serviceProvider.getName());
        //The status for the delivery should not have changed
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(deliveryCapabilityMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());

    }


    @Test
    public void updateDeliveryStatusShouldMakeDeliveryNoOverlapIfNoCapabilitiesMatch() {
        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "/deliveries/1",
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
        when(deliveryCapabilityMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus(serviceProvider.getName());
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(deliveryCapabilityMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());
    }
}
