package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.service.exportmodel.ExportTransformer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;


public class ImportModelDocumentationTest {

    public ObjectMapper mapper = new ObjectMapper();

    public ExportTransformer exportTransformer = new ExportTransformer();

    public NeighbourImportApi getNeighbourImportApi() {
        NeighbourCapabilityImportApi capability = new NeighbourCapabilityImportApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000:NPRA_DATEX_1",
                        "NO",
                        "DATEX:1.0",
                        Collections.singletonList("100302"),
                        "SituationPublication",
                        "NPRA"
                ),
                new MetadataImportApi(
                        1,
                        "NPRA.info.eu",
                        MetadataImportApi.RedirectStatusImportApi.OPTIONAL,
                        100,
                        100,
                        10
                )
        );

        NeighbourCapabilitiesImportApi capabilities = new NeighbourCapabilitiesImportApi(
                exportTransformer.transformLocalDateTimeToEpochMili(LocalDateTime.now()),
                NeighbourCapabilitiesImportApi.CapabilitiesStatusImportApi.KNOWN,
                Collections.singleton(capability),
                exportTransformer.transformLocalDateTimeToEpochMili(LocalDateTime.now())
        );

        NeighbourEndpointImportApi neighbourEndpoint = new NeighbourEndpointImportApi(
                "sub-" + UUID.randomUUID(),
                "amqps://my-interchange.eu",
                5671,
                100,
                100
        );

        String uuid = UUID.randomUUID().toString();
        NeighbourSubscriptionImportApi neighbourSubscription = new NeighbourSubscriptionImportApi(
                uuid,
                NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.CREATED,
                "originatingCountry = 'NO'",
                "my-interchange.eu/subscriptions/" + uuid,
                "my-neighbour.eu",
                Collections.singleton(neighbourEndpoint)
        );

        EndpointImportApi endpoint = new EndpointImportApi(
                UUID.randomUUID().toString(),
                "amqps://my-neighbour.eu",
                5671,
                100,
                100
        );

        SubscriptionImportApi subscription = new SubscriptionImportApi(
                "publicationId = 'NO00000:NPRA_DATEX_1'",
                "my-neighbour.eu/subscriptions/" + UUID.randomUUID(),
                SubscriptionImportApi.SubscriptionStatusImportApi.CREATED,
                "my-interchange.eu",
                Collections.singleton(endpoint)
        );

        return new NeighbourImportApi(
                "my-neighbour",
                capabilities,
                Collections.singleton(neighbourSubscription),
                Collections.singleton(subscription),
                "443"
        );
    }

    public ServiceProviderImportApi getServiceProviderImportApi() {
        String capShard = "cap-" + UUID.randomUUID();
        String localQueue = "loc-" + UUID.randomUUID();
        LocalEndpointImportApi subEndpoint = new LocalEndpointImportApi(
                "amqps://my-interchange.eu",
                5671,
                localQueue,
                100,
                100
        );

        LocalConnectionImportApi localConnection = new LocalConnectionImportApi(
                capShard,
                localQueue
        );

        LocalSubscriptionImportApi localSubscription = new LocalSubscriptionImportApi(
                UUID.randomUUID().toString(),
                "publicationId = 'NO00000:NPRA_DATEX_1' OR publicationId = 'NO00000:NPRA_DATEX_2'",
                "my-interchange.eu",
                LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.CREATED,
                Collections.singleton(subEndpoint),
                Collections.singleton(localConnection)
        );

        CapabilityShardImportApi capabilityShard = new CapabilityShardImportApi(
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

        CapabilityImportApi capability = new CapabilityImportApi(
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
                new MetadataImportApi(
                        1,
                        "NPRA.info.eu",
                        MetadataImportApi.RedirectStatusImportApi.OPTIONAL,
                        100,
                        100,
                        10
                )
        );
        capability.setShards(Collections.singleton(capabilityShard));
        capability.setStatus(CapabilityImportApi.CapabilityStatusImportApi.CREATED);

        DeliveryEndpointImportApi deliveryendpoint = new DeliveryEndpointImportApi(
                "amqps://my-interchange.eu",
                5671,
                "del-" + UUID.randomUUID(),
                100,
                100
        );

        DeliveryImportApi delivery = new DeliveryImportApi(
                UUID.randomUUID().toString(),
                Collections.singleton(deliveryendpoint),
                "publicationId = 'NO00000:NPRA_DATEX_2",
                DeliveryImportApi.DeliveryStatusImportApi.CREATED
        );

        return new ServiceProviderImportApi(
                "my-service-provider",
                Collections.singleton(localSubscription),
                Collections.singleton(capability),
                Collections.singleton(delivery),
                exportTransformer.transformLocalDateTimeToEpochMili(LocalDateTime.now())
        );
    }

    public PrivateChannelImportApi getPrivateChannelImportApi() {
        return new PrivateChannelImportApi(
                UUID.randomUUID().toString(),
                "my-service-provider",
                "other-service-provider",
                PrivateChannelImportApi.PrivateChannelStatusImportApi.CREATED,
                new PrivateChannelEndpointImportApi(
                        "amqps://my-interchange.eu",
                        5671,
                        "priv-" + UUID.randomUUID()
                )
        );
    }

    @Test
    public void printNeighbourImportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNeighbourImportApi()));
    }

    @Test
    public void printServiceProviderImportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getServiceProviderImportApi()));
    }

    @Test
    public void printPrivateChannelImportApi() throws JsonProcessingException {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getPrivateChannelImportApi()));
    }

    @Test
    public void printImportApi() throws JsonProcessingException {
        ImportApi importApi = new ImportApi(
                Collections.singleton(getNeighbourImportApi()),
                Collections.singleton(getServiceProviderImportApi()),
                Collections.singleton(getPrivateChannelImportApi())
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(importApi));
    }
}