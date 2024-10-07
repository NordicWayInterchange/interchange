package no.vegvesen.ixn.federation.service.exportmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public class ExportModelDocumentationTest {

    public ObjectMapper mapper = new ObjectMapper();

    public ExportTransformer exportTransformer = new ExportTransformer();

    public NeighbourExportApi getNeighbourExportApi() {
        NeighbourCapabilityExportApi capability = new NeighbourCapabilityExportApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000:NPRA_DATEX_1",
                        "NO",
                        "DATEX:1.0",
                        Collections.singletonList("100302"),
                        "SituationPublication",
                        "NPRA"
                ),
                new MetadataExportApi(
                        1,
                        "NPRA.info.eu",
                        MetadataExportApi.RedirectStatusExportApi.OPTIONAL,
                        100,
                        100,
                        10
                )
        );

        NeighbourCapabilitiesExportApi capabilities = new NeighbourCapabilitiesExportApi(
                exportTransformer.transformLocalDateTimeToEpochMili(LocalDateTime.now()),
                Collections.singleton(capability),
                NeighbourCapabilitiesExportApi.NeighbourCapabilitiesStatusExportApi.KNOWN,
                exportTransformer.transformLocalDateTimeToEpochMili(LocalDateTime.now())
        );

        NeighbourEndpointExportApi neighbourEndpoint = new NeighbourEndpointExportApi(
                "sub-" + UUID.randomUUID(),
                "amqps://my-interchange.eu",
                5671,
                100,
                100
        );

        String uuid = UUID.randomUUID().toString();
        NeighbourSubscriptionExportApi neighbourSubscription = new NeighbourSubscriptionExportApi(
                uuid,
                NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.CREATED,
                "originatingCountry = 'NO'",
                "my-interchange.eu/subscriptions/" + uuid,
                "my-neighbour.eu",
                Collections.singleton(neighbourEndpoint)
        );

        EndpointExportApi endpoint = new EndpointExportApi(
                UUID.randomUUID().toString(),
                "amqps://my-neighbour.eu",
                5671,
                100,
                100
        );

        SubscriptionExportApi subscription = new SubscriptionExportApi(
                "my-neighbour.eu/subscriptions/" + UUID.randomUUID(),
                "publicationId = 'NO00000:NPRA_DATEX_1'",
                "my-interchange.eu",
                SubscriptionExportApi.SubscriptionStatusExportApi.CREATED,
                Collections.singleton(endpoint)
        );

        return new NeighbourExportApi(
                "my-neighbour",
                capabilities,
                Collections.singleton(neighbourSubscription),
                Collections.singleton(subscription),
                "443"
        );
    }

    public ServiceProviderExportApi getServiceProviderExportApi() {
        String capShard = "cap-" + UUID.randomUUID();
        String localQueue = "loc-" + UUID.randomUUID();
        LocalEndpointExportApi subEndpoint = new LocalEndpointExportApi(
                "amqps://my-interchange.eu",
                5671,
                localQueue,
                100,
                100
        );

        LocalConnectionExportApi localConnection = new LocalConnectionExportApi(
                capShard,
                localQueue
        );

        LocalSubscriptionExportApi localSubscription = new LocalSubscriptionExportApi(
                UUID.randomUUID().toString(),
                "publicationId = 'NO00000:NPRA_DATEX_1' OR publicationId = 'NO00000:NPRA_DATEX_2'",
                "my-interchange.eu",
                LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.CREATED,
                Collections.singleton(subEndpoint),
                Collections.singleton(localConnection)
        );

        CapabilityShardExportApi capabilityShard = new CapabilityShardExportApi(
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

        CapabilityExportApi capability = new CapabilityExportApi(
                UUID.randomUUID().toString(),
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000:NPRA_DATEX_2",
                        "NO",
                        "DATEX:1.0",
                        Collections.singletonList("100303"),
                        "SituationPublication",
                        "NPRA"
                ),
                new MetadataExportApi(
                        1,
                        "NPRA.info.eu",
                        MetadataExportApi.RedirectStatusExportApi.OPTIONAL,
                        100,
                        100,
                        10
                ),
                CapabilityExportApi.CapabilityStatusExportApi.CREATED,
                Collections.singleton(capabilityShard)
        );

        DeliveryEndpointExportApi deliveryendpoint = new DeliveryEndpointExportApi(
                "amqps://my-interchange.eu",
                5671,
                "del-" + UUID.randomUUID(),
                100,
                100
        );

        DeliveryExportApi delivery = new DeliveryExportApi(
                UUID.randomUUID().toString(),
                Collections.singleton(deliveryendpoint),
                "publicationId = 'NO00000:NPRA_DATEX_2",
                DeliveryExportApi.DeliveryStatusExportApi.CREATED
        );

        return new ServiceProviderExportApi(
                "my-service-provider",
                Collections.singleton(localSubscription),
                Collections.singleton(capability),
                Collections.singleton(delivery)
                //missing subscriptionUpdated
        );
    }

    public PrivateChannelExportApi getPrivateChannelExportApi() {
        return new PrivateChannelExportApi(
                UUID.randomUUID().toString(),
                "my-service-provider",
                "other-service-provider",
                PrivateChannelExportApi.PrivateChannelStatusExportApi.CREATED,
                new PrivateChannelEndpointExportApi(
                        "amqps://my-interchange.eu",
                        5671,
                        "priv-" + UUID.randomUUID()
                )
        );
    }

    @Test
    public void printNeighbourExportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNeighbourExportApi()));
    }

    @Test
    public void printServiceProviderExportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getServiceProviderExportApi()));
    }

    @Test
    public void printPrivateChannelExportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getPrivateChannelExportApi()));
    }

    @Test
    public void printExportApi() throws JsonProcessingException {
        ExportApi exportApi = new ExportApi(
                Collections.singleton(getNeighbourExportApi()),
                Collections.singleton(getServiceProviderExportApi()),
                Collections.singleton(getPrivateChannelExportApi())
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportApi));
    }
}