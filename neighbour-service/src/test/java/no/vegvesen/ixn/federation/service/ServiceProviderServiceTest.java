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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
