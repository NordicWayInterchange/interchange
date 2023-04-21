package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Mock
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Mock
    MatchDiscoveryService matchDiscoveryService;

    ServiceProviderService service;

    @BeforeEach
    void setUp() {
        service = new ServiceProviderService(serviceProviderRepository, outgoingMatchDiscoveryService, matchDiscoveryService);
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

        service.updateNewLocalDeliveryEndpoints(sp, "host", 5671);

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

        service.updateNewLocalDeliveryEndpoints(sp, "host", 5671);

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

        service.updateNewLocalDeliveryEndpoints(sp, "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(0);
    }
}
