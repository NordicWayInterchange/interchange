package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
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
import static org.junit.jupiter.api.Assertions.fail;
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

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("123"),
                        Collections.singleton(1)
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

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("123"),
                        Collections.singleton(1)
                ),
                new Metadata()
        );
        capability.setStatus(CapabilityStatus.CREATED);
        sp.getCapabilities().addDataType(capability);

        CapabilitySplit capability1 = new CapabilitySplit(
                new DenmApplication(
                        "publisher-1",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("122"),
                        Collections.singleton(1)
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
        CapabilitySplit capability = new CapabilitySplit(
                1,
                new DenmApplication(
                        "0001",
                        "0001:0001",
                        "NO",
                        "1.0",
                        Collections.singleton("0122"),
                        Collections.singleton(6)
                ),
                new Metadata()
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                new Capabilities(
                        CapabilitiesStatus.KNOWN,
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
        service.updateDeliveryStatus(serviceProvider.getName());
        //The status for the delivery should not have changed
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(outgoingMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());

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
        when(outgoingMatchRepository.findAllByLocalDelivery_Id(localDelivery.getId())).thenReturn(new ArrayList<>());
        service.updateDeliveryStatus(serviceProvider.getName());
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
        verify(serviceProviderRepository).findByName(serviceProvider.getName());
        verify(outgoingMatchRepository).findAllByLocalDelivery_Id(localDelivery.getId());
    }
}
