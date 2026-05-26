package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {"no.vegvesen.ixn.federation.repository","no.vegvesen.ixn.federation.model"})
@SpringBootTest(classes = {OutgoingMatchDiscoveryService.class, OutgoingMatchRepository.class, ServiceProviderRepository.class})
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
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "NO delivery", false);


        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
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
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap2.setStatus(CapabilityStatus.CREATED);


        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(cap1, cap2)),
            Set.of(),
                Set.of(delivery),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(1);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatMultipleMatchesAreCreated() {
        LocalDelivery delivery = new LocalDelivery("publisherId = 'NPRA'", LocalDeliveryStatus.REQUESTED, "NPRA DELIVERY", false);


        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
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
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap2.setStatus(CapabilityStatus.CREATED);
        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(cap1, cap2)),
                Set.of(),
                Set.of(delivery),
                LocalDateTime.now()
        );


        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(2);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void testThatDeliveryHasNoOverlap() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'DE'", LocalDeliveryStatus.REQUESTED, "DE delivery", true);


        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
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
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap2.setStatus(CapabilityStatus.CREATED);

        Capabilities capabilities = new Capabilities(Set.of(cap1, cap2));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                capabilities,
                Set.of(),
                Set.of(delivery),
                LocalDateTime.now()

        );
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(0);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void deliveryStatusIsNotChangedWhenStatusIsIllegal() {
        LocalDelivery delivery1 = new LocalDelivery("", LocalDeliveryStatus.ILLEGAL, "Illegal delivery", false);
        LocalDelivery delivery2 = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "No delivery", false);

        ServiceProvider serviceProvider = new ServiceProvider(
                "service-provider",
                new Capabilities(),
                Set.of(),
                Set.of(delivery1,delivery2),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);

        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Collections.singletonList(serviceProvider));
        assertThat(repository.findAll()).hasSize(0);
    }

    @Test
    public void matchesAreOnlyCreatedWhenCapabilityIsCreated() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "Delivery", false);


        Capability cap1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singletonList("1234"),
                        Collections.singletonList(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap1.setStatus(CapabilityStatus.CREATED);

        Capability cap2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-2",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singletonList("1234"),
                        Collections.singletonList(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap2.setStatus(CapabilityStatus.REQUESTED);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(cap1, cap2)),
               Set.of(),
                Set.of(delivery),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(1);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void matchIsNotCreatedWhenCapabilityIsNotShardedAndLocalDeliveryIsSharded() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' AND shardId = 2", LocalDeliveryStatus.REQUESTED, "Delivery", false);


        Capability cap = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singletonList("1234"),
                        Collections.singletonList(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );
        cap.setStatus(CapabilityStatus.CREATED);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(cap)),
               Set.of(),
               Set.of(delivery),
               LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(0);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void matchIsCreatedWhenCapabilityIsShardedAndLocalDeliveryIsNotSharded() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "Delivery", false);


        Capability cap = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singletonList("1234"),
                        Collections.singletonList(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.<CapabilityShard>of(
                    new CapabilityShard(1, "cap-" + UUID.randomUUID(), null),
                    new CapabilityShard(2, "cap-" + UUID.randomUUID(), null)
                )
        );
        cap.setStatus(CapabilityStatus.CREATED);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                 new Capabilities(Set.of(cap)),
                Set.of(),
                Set.of(delivery),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);
        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Arrays.asList(serviceProvider));

        assertThat(repository.findAll()).hasSize(1);

        //clean-up
        repository.deleteAll();
        serviceProviderRepository.deleteAll();
    }
}
