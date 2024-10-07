package no.vegvesen.ixn.federation.service;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ImportExportApplicationIT extends PostgresContainerBase {

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    PrivateChannelRepository privateChannelRepository;

    @Autowired
    ImportExportApplication application;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("localPath", ()-> "src/test/resources/importExportDump.json");
    }

    @Test
    public void exportModel() throws Exception {
        neighbourRepository.save(getNeighbour());
        serviceProviderRepository.save(getServiceProvider());
        privateChannelRepository.save(getPrivateChannel());
        application.exportModel();
    }

    @Test
    public void importModel() throws Exception {
        application.importModelWithNeighbours();

        assertThat(neighbourRepository.findAll()).hasSize(1);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(privateChannelRepository.findAll()).hasSize(1);
    }

    public Neighbour getNeighbour() {
        NeighbourCapability capability = new NeighbourCapability(
                new DatexApplication(
                        "NO00000",
                        "NO00000:NPRA_DATEX_1",
                        "NO",
                        "DATEX:1.0",
                        Collections.singletonList("100302"),
                        "SituationPublication",
                        "NPRA"
                ),
                new Metadata(
                        "NPRA.info.eu",
                        1,
                        RedirectStatus.OPTIONAL,
                        100,
                        100,
                        10
                )
        );

        NeighbourCapabilities capabilities = new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                Collections.singleton(capability),
                LocalDateTime.now()
        );

        NeighbourEndpoint neighbourEndpoint = new NeighbourEndpoint(
                "sub-" + UUID.randomUUID(),
                "amqps://my-interchange.eu",
                5671,
                100,
                100
        );

        String uuid = UUID.randomUUID().toString();
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription(
                uuid,
                NeighbourSubscriptionStatus.CREATED,
                "originatingCountry = 'NO'",
                "my-interchange.eu/subscriptions/" + uuid,
                "my-neighbour.eu",
                Collections.singleton(neighbourEndpoint)
        );

        Endpoint endpoint = new Endpoint(
                UUID.randomUUID().toString(),
                "amqps://my-neighbour.eu",
                5671,
                100,
                100
        );

        Subscription subscription = new Subscription(
                SubscriptionStatus.CREATED,
                "publicationId = 'NO00000:NPRA_DATEX_1'",
                "my-neighbour.eu/subscriptions/" + UUID.randomUUID(),
                "my-interchange.eu",
                Collections.singleton(endpoint)
        );

        return new Neighbour(
                "my-neighbour",
                capabilities,
                new NeighbourSubscriptionRequest(Collections.singleton(neighbourSubscription)),
                new SubscriptionRequest(Collections.singleton(subscription))
        );
    }

    public ServiceProvider getServiceProvider() {
        String capShard = "cap-" + UUID.randomUUID();
        String localQueue = "loc-" + UUID.randomUUID();
        LocalEndpoint subEndpoint = new LocalEndpoint(
                localQueue,
                "amqps://my-interchange.eu",
                5671,
                100,
                100
        );

        LocalConnection localConnection = new LocalConnection(
                capShard,
                localQueue
        );

        LocalSubscription localSubscription = new LocalSubscription(
                UUID.randomUUID().toString(),
                LocalSubscriptionStatus.CREATED,
                "publicationId = 'NO00000:NPRA_DATEX_1' OR publicationId = 'NO00000:NPRA_DATEX_2'",
                "my-interchange.eu",
                Collections.singleton(localConnection),
                Collections.singleton(subEndpoint)
        );

        Shard capabilityShard = new Shard(
                1,
                capShard,
                "publisherId = 'NO00000' AND " +
                        "publicationId = 'NO00000:NPRA_DATEX_2' AND " +
                        "originatingCountry = 'NO' AND " +
                        "protocolVersion = 'DATEX:1.0' AND " +
                        "quadTree like '%,100303%' AND " +
                        "publicationType = 'SituationPublication' AND " +
                        "publisherName = 'NPRA'"
        );

        Capability capability = new Capability(
                UUID.randomUUID().toString(),
                new DatexApplication(
                        "NO00000",
                        "NO00000:NPRA_DATEX_2",
                        "NO",
                        "DATEX:1.0",
                        Collections.singletonList("100303"),
                        "SituationPublication",
                        "NPRA"
                ),
                new Metadata(
                        "NPRA.info.eu",
                        1,
                        RedirectStatus.OPTIONAL,
                        100,
                        100,
                        10
                )
        );
        capability.getMetadata().setShards(Collections.singletonList(capabilityShard));
        capability.setStatus(CapabilityStatus.CREATED);

        LocalDeliveryEndpoint deliveryendpoint = new LocalDeliveryEndpoint(
                "amqps://my-interchange.eu",
                5671,
                "del-" + UUID.randomUUID(),
                100,
                100
        );

        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Collections.singleton(deliveryendpoint),
                "publicationId = 'NO00000:NPRA_DATEX_2",
                LocalDeliveryStatus.CREATED
        );

        return new ServiceProvider(
                "my-service-provider",
                new Capabilities(Collections.singleton(capability)),
                Collections.singleton(localSubscription),
                Collections.singleton(delivery),
                LocalDateTime.now()
        );
    }

    public PrivateChannel getPrivateChannel() {
        return new PrivateChannel(
                UUID.randomUUID().toString(),
                "other-service-provider",
                PrivateChannelStatus.CREATED,
                new PrivateChannelEndpoint(
                        "amqps://my-interchange.eu",
                        5671,
                        "priv-" + UUID.randomUUID()
                ),
                "my-service-provider"
        );
    }
}