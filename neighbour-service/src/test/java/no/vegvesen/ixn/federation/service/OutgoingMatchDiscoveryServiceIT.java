package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class OutgoingMatchDiscoveryServiceIT {

    @Autowired
    private OutgoingMatchRepository repository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private OutgoingMatchDiscoveryService service;

    @Test
    public void serviceIsAutowired() {
        assertThat(service).isNotNull();
    }

    @Test
    public void testThatMatchIsCreated() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

        Capability cap1 = new DenmCapability(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("6")
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new DenmCapability(
                "NPRA",
                "SE",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("5")
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatMultipleMatchesAreCreated() {
        LocalDelivery delivery = new LocalDelivery("publisherId = 'NPRA'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

        Capability cap1 = new DenmCapability(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("6")
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new DenmCapability(
                "NPRA",
                "SE",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("5")
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(2);
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatDeliveryHasNoOverlap() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'DE'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

        Capability cap1 = new DenmCapability(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("6")
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new DenmCapability(
                "NPRA",
                "SE",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("5")
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(0);
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void deliveryStatusIsNotChangedWhenStatusIsIllegal() {
        LocalDelivery delivery = new LocalDelivery("", LocalDeliveryStatus.ILLEGAL);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);

        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Collections.singletonList(serviceProvider));
        assertThat(repository.findAll()).hasSize(0);
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.ILLEGAL);
    }
}
