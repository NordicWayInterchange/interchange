package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional //TODO this is needed because the inserted data survives between tests. This is due to the ClassRule annotation being inside the PostgresTestcontainerInitializer class, and not the actual test class, I think.
public class ExportServiceProvidersIT {


    @Autowired
    ServiceProviderRepository repository;
    @Autowired
    PrivateChannelRepository privateChannelRepository;


    @Test
    public void getServiceProviders() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                "testuser",
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
                                        new Metadata(
                                                RedirectStatus.OPTIONAL
                                        )
                                )
                        )
                ),
                Collections.singleton(
                        new LocalSubscription(
                                1,
                                LocalSubscriptionStatus.CREATED,
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "my-node")
                ),
                Collections.singleton(
                        new LocalDelivery(
                                1,
                                Collections.singleton(
                                        new LocalDeliveryEndpoint(
                                                1,
                                                "myHost",
                                                123,
                                                "target"
                                        )
                                ),
                                "/a/b/c",
                                "a = b",
                                LocalDeliveryStatus.CREATED
                        )
                ),
                LocalDateTime.now()
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

        List<ServiceProvider> serviceProviderList = repository.findAll();
        Iterable<PrivateChannel> privateChannelList = privateChannelRepository.findAll();
        ExportApi exportApi = ServiceProviderImport.getExportApi(serviceProviderList,privateChannelList);

        assertThat(exportApi.getPrivateChannels().size()).isEqualTo(1);
        assertThat(exportApi.getPrivateChannels()).allMatch( p -> Objects.equals(p.getEndpoint().getHost(),"my-host"));
        assertThat(exportApi.getServiceProviders().size()).isEqualTo(1);
        assertThat(exportApi.getServiceProviders()).allMatch(s -> Objects.equals(s.getName(),"testuser"));
    }

}
