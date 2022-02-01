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
        ServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getServiceProviderApis(path);
        for (ServiceProviderApi serviceProviderApi : serviceProviderApis) {
            ServiceProvider serviceProvider = new ServiceProvider(serviceProviderApi.getName());
            Set<Capability> capabilities = transformer.capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities());
            Capabilities capabilities1 = new Capabilities();
            capabilities1.setCapabilities(capabilities);
            serviceProvider.setCapabilities(capabilities1);
            Set<LocalActorSubscription> subscriptions = serviceProviderApi.getSubscriptions();
            for (LocalActorSubscription localActorSubscription : subscriptions) {

                serviceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                        localActorSubscription.getSelector(),
                        //localActorSubscription.isCreateNewQueue(), //createNewQueue is gone
                        serviceProviderApi.getName()));
            }

            repository.save(serviceProvider);
        }
        List<ServiceProvider> savedServiceProviders = repository.findAll();
        assertThat(serviceProviderApis.length).isEqualTo(savedServiceProviders.size());

    }
}
