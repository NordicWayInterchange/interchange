package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

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
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ImportServiceProvidersIT {


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
            Set<CapabilitySplit> capabilities = transformer.capabilitiesSplitApiToCapabilitiesSplit(serviceProviderApi.getCapabilities());
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
