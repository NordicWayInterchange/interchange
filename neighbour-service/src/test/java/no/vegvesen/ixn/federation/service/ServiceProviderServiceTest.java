package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Mock
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    ServiceProviderService service;

    @BeforeEach
    void setUp() {
        service = new ServiceProviderService(serviceProviderRepository, outgoingMatchDiscoveryService);
    }

    @Test
    public void updateDeliveryEndpointsAddsNewEndpoints() {
        ServiceProvider sp = new ServiceProvider("sp");
        String selector = "originatingCountry = 'NO'";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDateTime.now(),
                LocalDeliveryStatus.CREATED
        );
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

        OutgoingMatch match = new OutgoingMatch(localDelivery, capability, sp.getName(), "my-queue", OutgoingMatchStatus.UP);

        when(outgoingMatchDiscoveryService.findMatchesWithNewEndpoints(anyString(), anyInt())).thenReturn(Arrays.asList(match));

        service.updateNewLocalDeliveryEndpoints(sp, "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(1);
        verify(outgoingMatchDiscoveryService, times(1)).findMatchesWithNewEndpoints(anyString(), anyInt());
    }

    @Test
    public void updateMultipleDeliveryEndpointsAddsNewEndpoints() {
        ServiceProvider sp = new ServiceProvider("sp");
        String selector = "originatingCountry = 'NO'";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDateTime.now(),
                LocalDeliveryStatus.CREATED
        );
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

        OutgoingMatch match = new OutgoingMatch(localDelivery, capability, sp.getName(), "my-queue", OutgoingMatchStatus.UP);
        OutgoingMatch match1 = new OutgoingMatch(localDelivery, capability1, sp.getName(), "my-queue1", OutgoingMatchStatus.UP);

        when(outgoingMatchDiscoveryService.findMatchesWithNewEndpoints(anyString(), anyInt())).thenReturn(Arrays.asList(match, match1));

        service.updateNewLocalDeliveryEndpoints(sp, "host", 5671);

        assertThat(localDelivery.getEndpoints()).hasSize(2);
        verify(outgoingMatchDiscoveryService, times(1)).findMatchesWithNewEndpoints(anyString(), anyInt());
    }

    @Test
    public void doNotUpdateDeliveryEndpointsWhenThereAreNoDeliveries() {
        ServiceProvider sp = new ServiceProvider("sp");

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

        verify(outgoingMatchDiscoveryService, times(0)).findMatchesWithNewEndpoints(anyString(), anyInt());
    }
}
