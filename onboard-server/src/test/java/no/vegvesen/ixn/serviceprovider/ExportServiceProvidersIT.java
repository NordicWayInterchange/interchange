package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(initializers = {ExportServiceProvidersIT.Initializer.class})
public class ExportServiceProvidersIT {

    @Autowired
    ServiceProviderRepository repository;


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url: jdbc:postgresql://localhost:5432/federation",
                    "spring.datasource.username: federation",
                    "spring.datasource.password: federation",
                    "spring.datasource.driver-class-name: org.postgresql.Driver"
            ).applyTo(configurableApplicationContext.getEnvironment());        }
    }


    @Test
    @Disabled
    public void getServiceProviders() throws IOException {
        Path path = Paths.get("/pre_deliveries_database_dump/jsonDump.txt");
        List<ServiceProvider> serviceProviderList = repository.findAll();
        writeToFile(path, serviceProviderList);
    }

    public static void writeToFile(Path path, List<ServiceProvider> serviceProviderList) throws IOException {
        CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
        TypeTransformer transformer = new TypeTransformer();
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        Set<ServiceProviderApi> serviceProviders = new HashSet<>();
        for (ServiceProvider serviceProvider : serviceProviderList) {
            ServiceProviderApi serviceProviderApi = new ServiceProviderApi();
            serviceProviderApi.setName(serviceProvider.getName());
            Set<CapabilityApi> capabilityApis = capabilityApiTransformer.capabilitiesToCapabilityApis(serviceProvider.getCapabilities().getCapabilities());
            serviceProviderApi.setCapabilities(capabilityApis);
            Set<LocalActorSubscription> localActorSubscriptions = transformer.transformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(serviceProvider.getName(), serviceProvider.getSubscriptions());
            serviceProviderApi.setSubscriptions(localActorSubscriptions);
            serviceProviders.add(serviceProviderApi);
        }
        writer.writeValue(path.toFile(),serviceProviders);
    }


}
