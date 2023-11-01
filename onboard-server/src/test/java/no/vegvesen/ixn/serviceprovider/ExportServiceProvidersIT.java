package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.*;
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
import java.nio.file.Paths;
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
    @Autowired
    PrivateChannelRepository privateChannelRepository;


    @Test
    @Disabled
    public void getServiceProviders() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider("testuser");
        serviceProvider.setCapabilities(
                new Capabilities(
                        Capabilities.CapabilitiesStatus.UNKNOWN,
                        Collections.singleton(
                                new CapabilitySplit(
                                        new DenmApplication(
                                                "NO-0001",
                                                "pub-1",
                                                "NO",
                                                "1.0",
                                                Collections.singleton("123"),
                                                Collections.singleton(6)
                                        ),
                                        new Metadata()
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
        Path path = tempDir.resolve("output.json");
        List<ServiceProvider> serviceProviderList = repository.findAll();
        writeToFile(path, serviceProviderList, privateChannelRepository.findAll());

        ServiceProviderApi[] serviceProviderApis = ServiceProviderImport.getServiceProviderApis(path);
        assertThat(serviceProviderApis.length).isEqualTo(1);
    }

    public static void writeToFile(Path path, List<ServiceProvider> serviceProviderList, List<PrivateChannel> privateChannelList) throws IOException {


        CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
        TypeTransformer transformer = new TypeTransformer();
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        Set<ServiceProviderApi> serviceProviders = new HashSet<>();
        for (ServiceProvider serviceProvider : serviceProviderList) {
            ServiceProviderApi serviceProviderApi = new ServiceProviderApi();
            serviceProviderApi.setName(serviceProvider.getName());
            Set<CapabilitySplitApi> capabilityApis = capabilityApiTransformer.capabilitiesSplitToCapabilitiesSplitApi(serviceProvider.getCapabilities().getCapabilities());
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

            Set<PrivateChannelApi> privateChannels = new HashSet<>();
            for (PrivateChannel privateChannel : privateChannelList) {
                privateChannels.add(new PrivateChannelApi(privateChannel.getPeerName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()),new PrivateChannelEndpointApi(privateChannel.getEndpoint()), privateChannel.getId()));
            }
            serviceProviderApi.setPrivateChannels(privateChannels);
            serviceProviders.add(serviceProviderApi);
        }
        writer.writeValue(path.toFile(),serviceProviders);
    }


}
