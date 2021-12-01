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
                                Collections.singleton("1234"),
                                Collections.singleton("6")
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
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
}
