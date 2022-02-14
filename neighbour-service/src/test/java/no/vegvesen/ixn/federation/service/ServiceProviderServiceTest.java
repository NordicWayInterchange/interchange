package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Test
    public void updateServiceProviderDeliveryWithOnramp() {
        ServiceProviderService serviceProviderService = new ServiceProviderService(serviceProviderRepository);
        String selector = "originatingCountry = 'NO' AND protocolVersion = 'DENM:1.1.0' AND quadTree like '%,123%,' AND causeCode like '%,1,%'";
        String localNodeName = "local-node";
        String serviceProviderName = "sp-1";

        LocalDelivery localDelivery = new LocalDelivery(
                1,
                "",
                selector,
                LocalDateTime.now(),
                LocalDeliveryStatus.REQUESTED
        );
        DenmCapability capability = new DenmCapability(
                "publisher-1",
                "NO",
                "DENM:1.1.0",
                Collections.singleton("123"),
                Collections.singleton("1")
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(
                        Capabilities.CapabilitiesStatus.KNOWN,
                        Collections.singleton(capability)),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        );
        serviceProvider.addDeliveries(Collections.singleton(localDelivery));
        doReturn(Arrays.asList(serviceProvider)).when(serviceProviderRepository).findAll();
        serviceProviderService.updateLocalDeliveries(localNodeName, "5671");
        assertThat(serviceProvider.getDeliveries()).hasSize(1);
        LocalDelivery delivery = serviceProvider.getDeliveries().stream().findFirst().get();
        assertThat(delivery.getEndpoints())
                .hasSize(1)
                .allMatch(b -> b.getHost().equals(localNodeName))
                .allMatch(b -> b.getPort() == 5671)
                .allMatch(b -> b.getSelector().equals(selector))
                .allMatch(b -> b.getTarget().equals("onramp"));
        verify(serviceProviderRepository,times(1)).findAll();
        verify(serviceProviderRepository).save(any());
    }
}
