package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
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
import java.util.HashSet;

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

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider-1");

        CapabilitySplit cap1 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(6)
                ),
                new Metadata()
        );

        CapabilitySplit cap2 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "SE",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
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
    public void testThatMultipleMatchesAreNotCreated() {
        LocalDelivery delivery = new LocalDelivery("publisherId = 'NPRA'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider-2");

        CapabilitySplit cap1 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(6)
                ),
                new Metadata()
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        CapabilitySplit cap2 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "SE",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
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
    public void testThatDeliveryHasNoOverlap() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'DE'", LocalDeliveryStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider-3");

        CapabilitySplit cap1 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(6)
                ),
                new Metadata()
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        CapabilitySplit cap2 = new CapabilitySplit(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "SE",
                        "DENM:1.2.2",
                        Collections.singleton("1234"),
                        Collections.singleton(5)
                ),
                new Metadata()
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
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
