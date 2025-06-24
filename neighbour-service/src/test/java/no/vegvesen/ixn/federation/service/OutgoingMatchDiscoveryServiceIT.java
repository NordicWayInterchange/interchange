package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

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
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "NO delivery");


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
        LocalDelivery delivery = new LocalDelivery("publisherId = 'NPRA'", LocalDeliveryStatus.REQUESTED, "NPRA DELIVERY");


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
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'DE'", LocalDeliveryStatus.REQUESTED, "DE delivery");


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
        LocalDelivery delivery1 = new LocalDelivery("", LocalDeliveryStatus.ILLEGAL, "Illegal delivery");
        LocalDelivery delivery2 = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "No delivery");

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        serviceProvider.setDeliveries(new HashSet<>(Arrays.asList(delivery1, delivery2)));
        serviceProviderRepository.save(serviceProvider);

        service.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(Collections.singletonList(serviceProvider));
        assertThat(repository.findAll()).hasSize(0);
    }

    @Test
    public void matchesAreOnlyCreatedWhenCapabilityIsCreated() {
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "Delivery");


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
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' AND shardId = 2", LocalDeliveryStatus.REQUESTED, "Delivery");


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
        LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.REQUESTED, "Delivery");


        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
        metadata.setShardCount(2);

        Capability cap = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singletonList("1234"),
                        Collections.singletonList(6)
                ),
                metadata
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
