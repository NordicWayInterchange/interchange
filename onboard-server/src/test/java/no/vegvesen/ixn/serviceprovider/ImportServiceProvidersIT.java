package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ImportServiceProvidersIT {

    @Autowired
    ServiceProviderRepository repository;

    //TODO private Channels
    @Test
    @Disabled
    public void importServiceProviders() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        OldServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getOldServiceProviderApis(path);
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
                        serviceProviderApi.getName())); //already have the user from the Service provider
            }

            repository.save(serviceProvider);
        }
        List<ServiceProvider> savedServiceProviders = repository.findAll();
        assertThat(serviceProviderApis.length).isEqualTo(savedServiceProviders.size());

    }
}
