package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.ContainerConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@Testcontainers
@Import(ContainerConfig.class)
public class ImportServiceProvidersIT {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = ContainerConfig.postgreSQLContainer();

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
    }

    @Autowired
    ServiceProviderRepository repository;

    @Autowired
    PrivateChannelRepository privateChannelRepository;

    @Test
    @Disabled
    public void importServiceProviders() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        OldServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getOldServiceProviderApis(Files.newInputStream(path));
        for (OldServiceProviderApi serviceProviderApi : serviceProviderApis) {
            ServiceProvider serviceProvider = new ServiceProvider(serviceProviderApi.getName());
            Set<Capability> capabilities = transformer.capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities());
            Capabilities capabilities1 = new Capabilities();
            capabilities1.setCapabilities(capabilities);
            serviceProvider.setCapabilities(capabilities1);
            Set<OldLocalActorSubscription> subscriptions = serviceProviderApi.getSubscriptions();
            for (OldLocalActorSubscription localActorSubscription : subscriptions) {
                //TODO have to generate queue name, as this was SP name before
                serviceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                        localActorSubscription.getSelector(),
                       "my-interchange" )); //already have the user from the Service provider
            }
            repository.save(serviceProvider);
        }
        List<ServiceProvider> savedServiceProviders = repository.findAll();
        assertThat(serviceProviderApis.length).isEqualTo(savedServiceProviders.size());

    }

    @Test
    @Disabled
    public void importServiceProvidersWithDeliveryEndpoints() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        OldServiceProviderApi[] serviceProviders = ServiceProviderImport.getOldServiceProviderApis(Files.newInputStream(path));
        List<ServiceProvider> serviceProvidersToSave = new ArrayList<>();
        List<PrivateChannel> privateChannelsToSave = new ArrayList<>();
        LocalDateTime saveTime = LocalDateTime.now();
        for (OldServiceProviderApi serviceProviderApi : serviceProviders) {
            ServiceProvider serviceProvider = ServiceProviderImport.mapOldServiceProviderApiToServiceProvider(serviceProviderApi, saveTime);
            serviceProvidersToSave.add(serviceProvider);

            List<PrivateChannel> privateChannels = ServiceProviderImport.mapPrivateChannelApiToPrivateChannels(serviceProviderApi.getName(), serviceProviderApi.getPrivateChannels());
            privateChannelsToSave.addAll(privateChannels);
        }
        repository.saveAll(serviceProvidersToSave);
        privateChannelRepository.saveAll(privateChannelsToSave);
    }


}
