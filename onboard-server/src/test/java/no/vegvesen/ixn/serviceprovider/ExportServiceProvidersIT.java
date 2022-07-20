package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.Delivery;
import no.vegvesen.ixn.serviceprovider.model.GetSubscriptionResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ExportServiceProvidersIT {

    @TempDir
    Path tempDir;

    @Autowired
    ServiceProviderRepository repository;


    @Test
    @Disabled
    public void getServiceProviders() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider("testuser");
        serviceProvider.setCapabilities(
                new Capabilities(
                        Capabilities.CapabilitiesStatus.UNKNOWN,
                        Collections.singleton(
                                new DatexCapability(
                                        "NO-0001",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("123"),
                                        Collections.singleton("6")
                                )
                        )
                )
        );
        serviceProvider.setSubscriptions(Collections.singleton(
                new LocalSubscription(
                        LocalSubscriptionStatus.CREATED,
                        "originatingCountry = 'NO' and messageType = 'DENM'")
                )
        );
        repository.save(serviceProvider);
        Path path = tempDir.resolve("output.json");
        List<ServiceProvider> serviceProviderList = repository.findAll();
        writeToFile(path, serviceProviderList);

        ServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getServiceProviderApis(path);
        assertThat(serviceProviderApis.length).isEqualTo(1);
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

            Set<GetSubscriptionResponse> spSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                spSubscriptions.add(transformer.transformLocalSubscriptionToGetSubscriptionResponse(serviceProvider.getName(),subscription));
            }
            serviceProviderApi.setSubscriptions(spSubscriptions);
            Set<Delivery> deliveries = transformer.transformLocalDeliveryToDelivery(serviceProvider.getName(),serviceProvider.getDeliveries());
            serviceProviderApi.setDeliveries(deliveries);
            serviceProviders.add(serviceProviderApi);
        }
        writer.writeValue(path.toFile(),serviceProviders);
    }


}
