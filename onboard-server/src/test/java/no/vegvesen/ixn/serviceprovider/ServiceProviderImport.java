package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.EndpointApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.DeliveryEndpoint;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelResponseApi;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public static ServiceProvider mapOldServiceProviderApiToServiceProvider(OldServiceProviderApi serviceProviderApi, LocalDateTime savedTimestamp) {
        Set<CapabilitySplit> capabilitySet = new CapabilityToCapabilityApiTransformer().capabilitiesSplitApiToCapabilitiesSplit(serviceProviderApi.getCapabilities());
        Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,capabilitySet);
        capabilities.setLastUpdated(LocalDateTime.now());
        Set<LocalSubscription> subscriptions = new HashSet<>();
        for (OldLocalActorSubscription subscriptionApi : serviceProviderApi.getSubscriptions()) {
            //TODO should we not import ILLEGAL local subscriptions?
            String selector = subscriptionApi.getSelector();
            if (!selector.trim().isEmpty()) {
                LocalSubscription localSubscription = new LocalSubscription(
                        LocalSubscriptionStatus.REQUESTED,
                        selector,
                        subscriptionApi.getConsumerCommonName()
                );
                if (! subscriptionApi.getEndpoints().isEmpty()) {
                    Set<LocalEndpoint> endpoints = new HashSet<>();
                    for (EndpointApi endpointApi : subscriptionApi.getEndpoints()) {
                        endpoints.add(new LocalEndpoint(
                                endpointApi.getSource(),
                                endpointApi.getHost(),
                                endpointApi.getPort(),
                                endpointApi.getMaxBandwidth(),
                                endpointApi.getMaxMessageRate()
                        ));
                    }
                    localSubscription.setLocalEndpoints(endpoints);
                }
                subscriptions.add(localSubscription);
            }
        }
        Set<LocalDelivery> deliveries = new HashSet<>();
        for (DeliveryApi deliveryApi : serviceProviderApi.getDeliveries()) {
            LocalDelivery delivery = new LocalDelivery(
                    deliveryApi.getSelector(),
                    LocalDeliveryStatus.REQUESTED
            );
            String exchangeName = null;
            for (DeliveryEndpoint endpoint : deliveryApi.getEndpoints()) {
                exchangeName = endpoint.getTarget();
            }

            if (exchangeName != null) {
                delivery.setExchangeName(exchangeName);
            }

            deliveries.add(delivery);
        }

        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderApi.getName(),
                capabilities,
                subscriptions,
                savedTimestamp
        );
        serviceProvider.addDeliveries(deliveries);
        return serviceProvider;
    }

    public static List<PrivateChannel> mapPrivateChannelApiToPrivateChannels(String serviceProviderName, Set<PrivateChannelResponseApi> privateChannelResponseApis) {
        List<PrivateChannel> importedPrivateChannels = new ArrayList<>();

        for (PrivateChannelResponseApi privateChannelResponseApi : privateChannelResponseApis) {
            importedPrivateChannels.add(new PrivateChannel(
                    privateChannelResponseApi.getPeerName(),
                    PrivateChannelStatus.REQUESTED,
                    new PrivateChannelEndpoint(
                            privateChannelResponseApi.getEndpoint().getHost(),
                            privateChannelResponseApi.getEndpoint().getPort(),
                            privateChannelResponseApi.getEndpoint().getQueueName()
                    ),
                    serviceProviderName
            ));
        }
        return importedPrivateChannels;
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
        /*
        Used to import to a locally runnning database
         */
    public static class LocalhostImportInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:5432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver",
                    "spring.jpa.hibernate.ddl-auto = update"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }
    /*
    Used to export a locally runnning database
     */
    public static class LocalhostExportInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:5432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }

}
