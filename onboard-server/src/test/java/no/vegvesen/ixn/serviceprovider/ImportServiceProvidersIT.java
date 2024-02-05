package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelApi;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelEndpointApi;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public void importServiceProviders() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsondump.json").toURI());
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        ImportApi importObject = ServiceProviderImport.getOldServiceProviderApis(Files.newInputStream(path));
        List<ServiceProvider> serviceProviders = new ArrayList<>();
        for (OldServiceProviderApi serviceProviderApi : importObject.getServiceProviders()) {
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
            //repository.save(serviceProvider);
            serviceProviders.add(serviceProvider);
        }
        List<PrivateChannel> privateChannels = new ArrayList<>();
        for (PrivateChannelImportExport oldPrivateChannel : importObject.getPrivateChannels()) {
            PrivateChannelEndpointApi oldEndpoint = oldPrivateChannel.getEndpoint();
            PrivateChannelEndpoint endpoint = null;
            if (oldEndpoint != null) {
                endpoint = new PrivateChannelEndpoint(
                        oldEndpoint.getHost(),
                        oldEndpoint.getPort(),
                        oldEndpoint.getQueueName()
                );

            }
            PrivateChannel privateChannel = new PrivateChannel(
                    oldPrivateChannel.getPeerName(),
                    PrivateChannelStatus.REQUESTED,
                    endpoint,
                    oldPrivateChannel.getServiceProvider()
            );
            privateChannels.add(privateChannel);
        }
        repository.saveAll(serviceProviders);
        privateChannelRepository.saveAll(privateChannels);

        List<ServiceProvider> savedServiceProviders = repository.findAll();
        List<PrivateChannel> savedPrivateChannels = StreamSupport.stream(privateChannelRepository.findAll().spliterator(),false).collect(Collectors.toList());
        assertThat(importObject.getServiceProviders().size()).isEqualTo(savedServiceProviders.size());
        assertThat(importObject.getPrivateChannels().size()).isEqualTo(savedPrivateChannels.size());

    }

    @Test
    @Disabled
    public void importServiceProvidersWithDeliveryEndpoints() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        ImportApi importApi = ServiceProviderImport.getOldServiceProviderApis(Files.newInputStream(path));
        List<ServiceProvider> serviceProvidersToSave = new ArrayList<>();
        List<PrivateChannel> privateChannelsToSave = new ArrayList<>();
        LocalDateTime saveTime = LocalDateTime.now();
        for (OldServiceProviderApi serviceProviderApi : importApi.getServiceProviders()) {
            ServiceProvider serviceProvider = ServiceProviderImport.mapOldServiceProviderApiToServiceProvider(serviceProviderApi, saveTime);
            serviceProvidersToSave.add(serviceProvider);

            List<PrivateChannel> privateChannels = ServiceProviderImport.mapPrivateChannelApiToPrivateChannels(serviceProviderApi.getName(), serviceProviderApi.getPrivateChannels());
            privateChannelsToSave.addAll(privateChannels);
        }
        repository.saveAll(serviceProvidersToSave);
        privateChannelRepository.saveAll(privateChannelsToSave);
    }


}
