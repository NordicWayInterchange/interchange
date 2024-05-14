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
import org.assertj.core.util.Streams;
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
import java.util.stream.Collectors;

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
    public void getServiceProviders() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider("testuser");
        serviceProvider.setCapabilities(
                new Capabilities(
                        CapabilitiesStatus.UNKNOWN,
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

            Set<PrivateChannel> serviceProviderPrivateChannelList = Streams.stream(privateChannelList).filter(p -> p.getServiceProviderName().equals(serviceProvider.getName())).collect(Collectors.toSet());
            Set<PrivateChannelResponseApi> privateChannels = new HashSet<>();
            for (PrivateChannel privateChannel : serviceProviderPrivateChannelList) {
                PrivateChannelEndpointApi endpointApi = new PrivateChannelEndpointApi(privateChannel.getEndpoint().getHost(),privateChannel.getEndpoint().getPort(),privateChannel.getEndpoint().getQueueName());
                privateChannels.add(new PrivateChannelResponseApi(privateChannel.getPeerName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()), endpointApi, privateChannel.getId()));
            }
            serviceProviderApi.setPrivateChannels(privateChannels);
            serviceProviders.add(serviceProviderApi);
        }
        System.out.println(writer.writeValueAsString(serviceProviders));
        writer.writeValue(path.toFile(),serviceProviders);
    }


}
