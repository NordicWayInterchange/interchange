package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ServiceProviderImport {
    public static ServiceProviderApi[] getServiceProviderApis(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceProviderApi[] serviceProviders = mapper.readValue(path.toFile(), ServiceProviderApi[].class);
        return serviceProviders;
    }

    public static OldServiceProviderApi[] getOldServiceProviderApis(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        OldServiceProviderApi[] serviceProviders = mapper.readValue(input,OldServiceProviderApi[].class);
        return serviceProviders;
    }

    public static ServiceProvider mapOldServiceProviderApiToServiceProvider(OldServiceProviderApi serviceProviderApi) {
        Set<Capability> capabilitySet = new CapabilityToCapabilityApiTransformer().capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities());
        Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,capabilitySet);
        Set<LocalSubscription> subscriptions = new HashSet<>();
        for (OldLocalActorSubscription subscriptionApi : serviceProviderApi.getSubscriptions()) {
            //TODO should we not import ILLEGAL local subscriptions?
            String selector = subscriptionApi.getSelector();
            if (!selector.trim().isEmpty()) {
                subscriptions.add(new LocalSubscription(
                        LocalSubscriptionStatus.REQUESTED,
                        selector
                ));
            }
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
        return serviceProvider;
    }

    /*
        Used to import data to systemtest, this one for the local instance
         */
    public static class LocalInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:15432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }

    /*
        Used to import data to systemtest, this one for the remote instance
         */
    public static class RemoteInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:25432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }
}
