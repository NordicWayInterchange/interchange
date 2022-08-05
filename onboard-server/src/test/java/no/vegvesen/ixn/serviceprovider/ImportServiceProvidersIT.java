package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {ImportServiceProvidersIT.LocalhostInitializer.class})
public class ImportServiceProvidersIT {


    public static class LocalhostInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:15432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }

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
                        localActorSubscription.getSelector())); //already have the user from the Service provider
            }
            repository.save(serviceProvider);
        }
        List<ServiceProvider> savedServiceProviders = repository.findAll();
        assertThat(serviceProviderApis.length).isEqualTo(savedServiceProviders.size());

    }

    @Test
    public void importServiceProvidersWithDeliveryEndpoints() throws IOException {
        Path path = Paths.get("C:\\interchange_dumps\\changed_dump","capability_with_delivery.json");
        OldServiceProviderApi[] serviceProviders = ServiceProviderImport.getOldServiceProviderApis(path);
        for (OldServiceProviderApi serviceProviderApi : serviceProviders) {
            //System.out.println(serviceProvider.getDeliveries().stream().map(getDeliveryResponse -> getDeliveryResponse.getEndpoints()).collect(Collectors.toSet()));
            System.out.println(serviceProviderApi);
            // repository.save(transformServiceProviderApiToServiceProvider(serviceProviderApi));
            Set<Capability> capabilitySet = new CapabilityToCapabilityApiTransformer().capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities());
            Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,capabilitySet);
            Set<LocalSubscription> subscriptions = new HashSet<>();
            for (OldLocalActorSubscription subscriptionApi : serviceProviderApi.getSubscriptions()) {
                LocalSubscription subscription = new LocalSubscription(
                        LocalSubscriptionStatus.valueOf(subscriptionApi.getStatus().toString()),
                        subscriptionApi.getSelector()
                );
                subscriptions.add(subscription);
            }
            Set<LocalDelivery> deliveries = new HashSet<>();
            for (DeliveryApi deliveryApi : serviceProviderApi.getDeliveries()) {
                LocalDelivery delivery = new LocalDelivery(
                        deliveryApi.getSelector(),
                        LocalDeliveryStatus.valueOf(deliveryApi.getStatus().name())
                );
                deliveries.add(delivery);
            }
            ServiceProvider serviceProvider = new ServiceProvider(serviceProviderApi.getName(),
                    capabilities,
                    subscriptions,
                    Collections.emptySet(),
                    LocalDateTime.now()
            );
            serviceProvider.addDeliveries(deliveries);
            repository.save(serviceProvider);
        }

    }

    /*
    private ServiceProvider transformServiceProviderApiToServiceProvider(OldServiceProviderApi serviceProviderApi) {
        CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
        return new ServiceProvider(
                serviceProviderApi.getName(),
                capabilityApiTransformer.capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities()),

        );
    }


     */
}
