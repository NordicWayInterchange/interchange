package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ApiModelRestDocumentationTest {

    @Test
    public void postCapabilitiesRequest() throws JsonProcessingException {
        CapabilitiesApi request = new CapabilitiesApi(
                "sp-1",
                Collections.singleton(
                        new CapabilityApi(
                                new DenmApplicationApi(
                                        "NPRA",
                                        "NO",
                                        "1.0",
                                        "DENM:1.2.2",
                                        List.of("1234"),
                                        List.of(6)
                                ),
                                new MetadataApi(
                                        1,
                                        "info.com",
                                        RedirectStatusApi.OPTIONAL,
                                        0,
                                        0,
                                        0
                                )
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void datexCapability() throws JsonProcessingException {
        CapabilityApi capabilityApi = new CapabilityApi(
                new DatexApplicationApi(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "DATEX:1.0",
                        List.of("1234"),
                        "Roadworks",
                        "publisherName"
                ),
                new MetadataApi(
                        1,
                        "https://my.capabilities-info.site/datex/roadworks",
                        RedirectStatusApi.OPTIONAL,
                        0,
                        0,
                        0
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilityApi));
    }

    @Test
    public void iviCapability() throws JsonProcessingException {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "IVIM:1.0",
                        List.of("1234")
                ),
                new MetadataApi(
                        1,
                        "https://my.capabilities-info.site/ivim/6",
                        RedirectStatusApi.OPTIONAL,
                        0,
                        0,
                        0
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));

    }

    @Test
    public void CapabilitiesResponse() throws JsonProcessingException {
        CapabilitiesApi response = new CapabilitiesApi(
                "",
                Collections.singleton(
                        new CapabilityApi(
                                new DenmApplicationApi(
                                        "NPRA",
                                        "pub-1",
                                        "NO",
                                        "DENM:1.2.2",
                                        List.of("1224"),
                                        List.of(5)
                                ),
                                new MetadataApi(
                                        1,
                                        "https://my.capabilities-info.site/info",
                                        RedirectStatusApi.OPTIONAL,
                                        0,
                                        0,
                                        0
                                )
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void postSubscriptionRequest() throws JsonProcessingException {
        SubscriptionRequestApi request = new SubscriptionRequestApi(
                "node-1",
                Collections.singleton(
                        new RequestedSubscriptionApi(
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "node-1"
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void subscriptionRequestResponse() throws JsonProcessingException {
        SubscriptionResponseApi response = new SubscriptionResponseApi(
                "node-1",
                Collections.singleton(
                        new RequestedSubscriptionResponseApi(
                                UUID.randomUUID().toString(),
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/1",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1"
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void subscriptionRequestResponseWithList() throws JsonProcessingException {
        SubscriptionResponseApi response = new SubscriptionResponseApi(
                "node-1",
                new HashSet<>(Arrays.asList(
                        new RequestedSubscriptionResponseApi(
                                UUID.randomUUID().toString(),
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/1",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1"
                        ),
                        new RequestedSubscriptionResponseApi(
                                UUID.randomUUID().toString(),
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/2",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1"
                        )
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void subscriptionPollResponse() throws JsonProcessingException {
        SubscriptionPollResponseApi response = new SubscriptionPollResponseApi(
                UUID.randomUUID().toString(),
                "originatingCountry = 'NO' and messageType = 'DENM'",
                "/subscription/1",
                SubscriptionStatusApi.CREATED,
                "node-1",
                Collections.singleton(
                        new EndpointApi(
                                "source-1",
                                "endpoint-1",
                                5671
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void testMetadataApplicationSplit() throws JsonProcessingException {
        ApplicationApi application = new ApplicationApi(
                Constants.DENM,
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                List.of("02312")
        );

        MetadataApi metadata = new MetadataApi(
                1,
                "https://info.no",
                RedirectStatusApi.OPTIONAL,
                0,
                0,
                0
        );

        CapabilityApi capabilityApi = new CapabilityApi(
                application,
                metadata
        );

        CapabilitiesApi capabilities = new CapabilitiesApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

    @Test
    public void testDenmMetadataApplicationSplit() throws JsonProcessingException {
        DenmApplicationApi application = new DenmApplicationApi(
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                List.of("02312"),
                List.of(6)
        );

        MetadataApi metadata = new MetadataApi(
                1,
                "https://info.no",
                RedirectStatusApi.OPTIONAL,
                0,
                0,
                0
        );

        CapabilityApi capabilityApi = new CapabilityApi(
                application,
                metadata
        );

        CapabilitiesApi capabilities = new CapabilitiesApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

    @Test
    public void testDenmMetadataApplicationSplitWithoutAllProps() throws JsonProcessingException {
        DenmApplicationApi application = new DenmApplicationApi(
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                List.of("02312"),
                List.of(6)
        );

        MetadataApi metadata = new MetadataApi();
        metadata.setInfoUrl("https://info.no");

        CapabilityApi capabilityApi = new CapabilityApi(
                application,
                metadata
        );

        CapabilitiesApi capabilities = new CapabilitiesApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

}
