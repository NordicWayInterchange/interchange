package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.EndpointApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.DeliveryEndpoint;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelResponseApi;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
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
        Set<Capability> capabilitySet = new CapabilityToCapabilityApiTransformer().capabilitiesApiToCapabilities(serviceProviderApi.getCapabilities());
        Capabilities capabilities = new Capabilities(capabilitySet);
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
                    LocalDeliveryStatus.REQUESTED,
                    "delivery"
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

    public static abstract class PostgreSQLContainerSetup{
        static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15").withDatabaseName("federation");

        @BeforeAll
        public static void setUp(){
            postgreSQLContainer.start();
        }
    }
    /*
        Used to import data to systemtest, this one for the local instance
         */
    public static abstract class LocalInitializer extends PostgreSQLContainerSetup {

        @DynamicPropertySource
        static void datasourceProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:15432/federation");
            registry.add("spring.datasource.username", () -> "federation");
            registry.add("spring.datasource.password", () -> "federation");
            registry.add("spring.datasource.driver-class-name", ()-> "org.postgresql.Driver");
        }
    }

    /*
        Used to import data to systemtest, this one for the remote instance
         */
    public static abstract class RemoteInitializer extends PostgreSQLContainerSetup{

        @DynamicPropertySource
        static void datasourceProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:25432/federation");
            registry.add("spring.datasource.username", () -> "federation");
            registry.add("spring.datasource.password", () -> "federation");
            registry.add("spring.datasource.driver-class-name", ()-> "org.postgresql.Driver");
        }
    }
        /*
        Used to import to a locally runnning database
         */
    public static abstract class LocalhostImportInitializer extends PostgreSQLContainerSetup{
            @DynamicPropertySource
            static void datasourceProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/federation");
                registry.add("spring.datasource.username", () -> "federation");
                registry.add("spring.datasource.password", () -> "federation");
                registry.add("spring.datasource.driver-class-name", ()-> "org.postgresql.Driver");
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
            }
    }
    /*
    Used to export a locally runnning database
     */
    public static abstract class LocalhostExportInitializer extends PostgreSQLContainerSetup{

        @DynamicPropertySource
        static void datasourceProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/federation");
            registry.add("spring.datasource.username", () -> "federation");
            registry.add("spring.datasource.password", () -> "federation");
            registry.add("spring.datasource.driver-class-name", ()-> "org.postgresql.Driver");
        }
    }

}
