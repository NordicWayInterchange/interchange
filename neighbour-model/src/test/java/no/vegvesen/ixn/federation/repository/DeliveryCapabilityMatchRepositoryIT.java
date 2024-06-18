package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class DeliveryCapabilityMatchRepositoryIT {

    @Autowired
    DeliveryCapabilityMatchRepository deliveryCapabilityMatchRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @AfterEach
    public void tearDown() {
        deliveryCapabilityMatchRepository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        assertThat(deliveryCapabilityMatchRepository.findAll()).isEmpty();
    }

    @Test
    public void saveServiceProviderBeforeSavingOutgoingMatchMatch() {
        LocalDelivery delivery = new LocalDelivery();

        DenmApplication app = new DenmApplication(
                "publisher-1",
                "publisher-1-0123",
                "NO",
                "DENM:1.1.0",
                List.of("123"),
                List.of(1)
        );

        Metadata meta = new Metadata();

        Capability cap = new Capability(app, meta);

        ServiceProvider sp = new ServiceProvider(
                "my-sp",
                new Capabilities(
                        Collections.singleton(cap)),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        );
        sp.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        serviceProviderRepository.save(sp);

        DeliveryCapabilityMatch match = new DeliveryCapabilityMatch(delivery, cap, "my-sp");
        deliveryCapabilityMatchRepository.save(match);

        List<DeliveryCapabilityMatch> allMatches = deliveryCapabilityMatchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    public void deletingOutgoingMatchFromDatabase() {
        LocalDelivery delivery = new LocalDelivery();

        DenmApplication app = new DenmApplication(
                "publisher-1",
                "publisher-1-0123",
                "NO",
                "DENM:1.1.0",
                List.of("123"),
                List.of(1)
        );

        Metadata meta = new Metadata();

        Capability cap = new Capability(app, meta);

        ServiceProvider sp = new ServiceProvider(
                "my-sp",
                new Capabilities(
                        Collections.singleton(cap)),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        );
        sp.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        serviceProviderRepository.save(sp);

        DeliveryCapabilityMatch match = new DeliveryCapabilityMatch(delivery, cap, "my-sp");
        deliveryCapabilityMatchRepository.save(match);

        deliveryCapabilityMatchRepository.delete(match);

        ServiceProvider savedSp = serviceProviderRepository.findByName("my-sp");
        assertThat(savedSp.getCapabilities().getCapabilities()).isNotEmpty();
        assertThat(savedSp.getDeliveries()).isNotEmpty();
    }

    @Test
    @Disabled
    public void removeCapabilityBeforeRemovingOutgoingMatch() {
        LocalDelivery delivery = new LocalDelivery();

        DenmApplication app = new DenmApplication(
                "publisher-1",
                "publisher-1-0123",
                "NO",
                "DENM:1.1.0",
                List.of("123"),
                List.of(1)
        );

        Metadata meta = new Metadata();

        Capability cap = new Capability(app, meta);

        ServiceProvider sp = new ServiceProvider(
                "my-sp",
                new Capabilities(
                        Collections.singleton(cap)),
                new HashSet<>(),
                new HashSet<>(),
                LocalDateTime.now()
        );
        sp.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
        serviceProviderRepository.save(sp);

        Integer capabilityId = sp.getCapabilities().getCapabilities().stream().findFirst().get().getId();

        DeliveryCapabilityMatch match = new DeliveryCapabilityMatch(delivery, cap, "my-sp");
        deliveryCapabilityMatchRepository.save(match);

        sp.setCapabilities(new Capabilities());
        serviceProviderRepository.save(sp);

        assertThat(deliveryCapabilityMatchRepository.findAll()).hasSize(1);
        assertThat(deliveryCapabilityMatchRepository.findAllByCapability_Id(capabilityId)).hasSize(0);
    }


}