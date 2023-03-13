package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Mock
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Mock
    MatchRepository matchRepository;

    ServiceProviderService service;

    @BeforeEach
    void setUp() {
        service = new ServiceProviderService(serviceProviderRepository, outgoingMatchDiscoveryService, matchRepository);
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

        DenmCapability capability = new DenmCapability(
                "publisher-1",
                "NO",
                "DENM:1.1.0",
                Collections.singleton("123"),
                Collections.singleton("1")
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

        DenmCapability capability = new DenmCapability(
                "publisher-1",
                "NO",
                "DENM:1.1.0",
                Collections.singleton("123"),
                Collections.singleton("1")
        );
        capability.setStatus(CapabilityStatus.CREATED);
        sp.getCapabilities().addDataType(capability);

        DenmCapability capability1 = new DenmCapability(
                "publisher-1",
                "NO",
                "DENM:1.1.0",
                Collections.singleton("122"),
                Collections.singleton("1")
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
