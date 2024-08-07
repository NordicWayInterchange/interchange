package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.ContainerConfig;
import no.vegvesen.ixn.postgresinit.PostgresContainerBase;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OutgoingMatchDiscoveryServiceIT extends PostgresContainerBase {

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

        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata()
        );

        Capability cap2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-2",
                        "SE",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(1);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatMultipleMatchesAreCreated() {
        LocalDelivery delivery = new LocalDelivery("publisherId = 'NPRA'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata()
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-12",
                        "SE",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(2);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatDeliveryHasNoOverlap() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'DE'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata()
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-2",
                        "SE",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Sets.newHashSet(Arrays.asList(cap1, cap2))));

        serviceProvider.setDeliveries(Collections.singleton(delivery));
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(0);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void deliveryStatusIsNotChangedWhenStatusIsIllegal() {
        LocalDelivery delivery1 = new LocalDelivery("", LocalDeliveryStatus.ILLEGAL);
        LocalDelivery delivery2 = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        serviceProvider.setDeliveries(new HashSet<>(Arrays.asList(delivery1, delivery2)));
        serviceProviderRepository.save(serviceProvider);

        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Collections.singletonList(serviceProvider));
        assertThat(repository.findAll()).hasSize(0);
    }

}
