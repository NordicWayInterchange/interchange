package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.ContainerConfig;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ExportServiceProvidersIT {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = ContainerConfig.postgreSQLContainer();

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
    }

    @TempDir
    Path tempDir;

    @Autowired
    ServiceProviderRepository repository;
    @Autowired
    PrivateChannelRepository privateChannelRepository;


    @Test
    public void getServiceProviders() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider("testuser");
        serviceProvider.setCapabilities(
                new Capabilities(
                        Collections.singleton(
                                new Capability(
                                        new DenmApplication(
                                                "NO-0001",
                                                "pub-1",
                                                "NO",
                                                "1.0",
                                                List.of("123"),
                                                List.of(6)
                                        ),
                                        new Metadata(
                                                RedirectStatus.OPTIONAL
                                        )
                                )
                        )
                )
        );
        serviceProvider.setSubscriptions(Collections.singleton(
                new LocalSubscription(
                        LocalSubscriptionStatus.CREATED,
                        "originatingCountry = 'NO' and messageType = 'DENM'",
                        "my-node")
                )
        );
        repository.save(serviceProvider);

        privateChannelRepository.save(new PrivateChannel(
                "my-peer",
                PrivateChannelStatus.CREATED,
                new PrivateChannelEndpoint(
                        "my-host",
                        5671,
                        "my-queue"
                ),
                serviceProvider.getName()
        ));

        Path path = tempDir.resolve("output.json");
        List<ServiceProvider> serviceProviderList = repository.findAll();
        Iterable<PrivateChannel> privateChannelList = privateChannelRepository.findAll();
        writeToFile(path, serviceProviderList, privateChannelList);

        ServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getServiceProviderApis(path);
        assertThat(serviceProviderApis.length).isEqualTo(1);
    }

    public static void writeToFile(Path path, List<ServiceProvider> serviceProviderList, Iterable<PrivateChannel> privateChannelList) throws IOException {
        CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
        TypeTransformer transformer = new TypeTransformer();
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        Set<ServiceProviderApi> serviceProviders = new HashSet<>();
        for (ServiceProvider serviceProvider : serviceProviderList) {
            ServiceProviderApi serviceProviderApi = new ServiceProviderApi();
            serviceProviderApi.setName(serviceProvider.getName());
            Set<CapabilityApi> capabilityApis = capabilityApiTransformer.capabilitiesToCapabilitiesApi(serviceProvider.getCapabilities().getCapabilities());
            serviceProviderApi.setCapabilities(capabilityApis);

            Set<GetSubscriptionResponse> spSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                spSubscriptions.add(transformer.transformLocalSubscriptionToGetSubscriptionResponse(serviceProvider.getName(),subscription));
            }
            serviceProviderApi.setSubscriptions(spSubscriptions);

            Set<GetDeliveryResponse> deliveries = new HashSet<>();
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                deliveries.add(transformer.transformLocalDeliveryToGetDeliveryResponse(serviceProvider.getName(),delivery));

            }
            serviceProviderApi.setDeliveries(deliveries);

            Set<PrivateChannel> serviceProviderPrivateChannelList = Streams.stream(privateChannelList).filter(p -> p.getServiceProviderName().equals(serviceProvider.getName())).collect(Collectors.toSet());
            Set<PrivateChannelResponseApi> privateChannels = new HashSet<>();
            for (PrivateChannel privateChannel : serviceProviderPrivateChannelList) {
                PrivateChannelEndpointApi endpointApi = new PrivateChannelEndpointApi(privateChannel.getEndpoint().getHost(),privateChannel.getEndpoint().getPort(),privateChannel.getEndpoint().getQueueName());
                privateChannels.add(new PrivateChannelResponseApi(privateChannel.getPeerName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()), endpointApi, privateChannel.getUuid()));
            }
            serviceProviderApi.setPrivateChannels(privateChannels);
            serviceProviders.add(serviceProviderApi);
        }
        System.out.println(writer.writeValueAsString(serviceProviders));
        writer.writeValue(path.toFile(),serviceProviders);
    }


}
