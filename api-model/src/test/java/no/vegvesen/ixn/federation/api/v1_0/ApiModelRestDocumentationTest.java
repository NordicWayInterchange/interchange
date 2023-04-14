package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ApiModelRestDocumentationTest {

    @Test
    public void postCapabilitiesRequest() throws JsonProcessingException {
        CapabilitiesApi request = new CapabilitiesApi(
                "sp-1",
                Collections.singleton(
                        new DenmCapabilityApi(
                                "NPRA",
                                "NO",
                                "1.0",
                                RedirectStatusApi.OPTIONAL,
                                1,
                                "https://my.capabilities-info.site/info",
                                Collections.singleton("1234"),
                                Collections.singleton("6")

                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void datexCapability() throws JsonProcessingException {
        DatexCapabilityApi datexCapabilityApi = new DatexCapabilityApi(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                RedirectStatusApi.OPTIONAL,
                1,
                "https://my.capabilities-info.site/datex/roadworks",
                Collections.singleton("RoadWorks")
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(datexCapabilityApi));
    }

    @Test
    public void iviCapability() throws JsonProcessingException {
        IvimCapabilityApi capability = new IvimCapabilityApi(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                RedirectStatusApi.OPTIONAL,
                1,
                "https://my.capabilities-info.site/ivim/6",
                Collections.singleton("6")
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));

    }

    @Test
    public void CapabilitiesResponse() throws JsonProcessingException {
        CapabilitiesApi response = new CapabilitiesApi(
                "sp-1",
                Collections.singleton(
                        new DenmCapabilityApi(
                                "NPRA",
                                "NO",
                                "1.0",
                                RedirectStatusApi.OPTIONAL,
                                1,
                                "https://my.capabilities-info.site/info",
                                Collections.singleton("1224"),
                                Collections.singleton("5")
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
                                "1",
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/1",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1",
                                System.currentTimeMillis()
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
                                "1",
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/1",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1",
                                System.currentTimeMillis()
                        ),
                        new RequestedSubscriptionResponseApi(
                                "2",
                                "originatingCountry = 'NO' and messageType = 'DENM'",
                                "/subscription/2",
                                SubscriptionStatusApi.REQUESTED,
                                "node-1",
                                System.currentTimeMillis()
                        )
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void subscriptionPollResponse() throws JsonProcessingException {
        SubscriptionPollResponseApi response = new SubscriptionPollResponseApi(
                "1",
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
        CapabilityApplicationApi application = new CapabilityApplicationApi(
                Constants.DENM,
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                new HashSet<>(Collections.singleton("02312"))
        );

        CapabilityMetadataApi metadata = new CapabilityMetadataApi(
                1,
                "https://info.no",
                RedirectStatusApi.OPTIONAL,
                0,
                0,
                0
        );

        CapabilitySplitApi capabilityApi = new CapabilitySplitApi(
                application,
                metadata
        );

        CapabilitiesSplitApi capabilities = new CapabilitiesSplitApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

    @Test
    public void testDenmMetadataApplicationSplit() throws JsonProcessingException {
        DenmCapabilityApplicationApi application = new DenmCapabilityApplicationApi(
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                new HashSet<>(Collections.singleton("02312")),
                new HashSet<>(Collections.singleton("6"))
        );

        CapabilityMetadataApi metadata = new CapabilityMetadataApi(
                1,
                "https://info.no",
                RedirectStatusApi.OPTIONAL,
                0,
                0,
                0
        );

        CapabilitySplitApi capabilityApi = new CapabilitySplitApi(
                application,
                metadata
        );

        CapabilitiesSplitApi capabilities = new CapabilitiesSplitApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

    @Test
    public void testDenmMetadataApplicationSplitWithoutAllProps() throws JsonProcessingException {
        DenmCapabilityApplicationApi application = new DenmCapabilityApplicationApi(
                "NO00000",
                "BOUV01",
                "NO",
                "DENM:1.3.1",
                new HashSet<>(Collections.singleton("02312")),
                new HashSet<>(Collections.singleton("6"))
        );

        CapabilityMetadataApi metadata = new CapabilityMetadataApi();
        metadata.setInfoUrl("https://info.no");

        CapabilitySplitApi capabilityApi = new CapabilitySplitApi(
                application,
                metadata
        );

        CapabilitiesSplitApi capabilities = new CapabilitiesSplitApi(
                "my-node",
                new HashSet<>(Collections.singleton(capabilityApi))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

}
