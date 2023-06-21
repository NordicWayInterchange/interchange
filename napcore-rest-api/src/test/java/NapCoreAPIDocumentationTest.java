import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class NapCoreAPIDocumentationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void addNapSubscriptionRequestTest() throws JsonProcessingException {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                "originatingCountry = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'");

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptionRequest));
    }

    @Test
    public void addNapSubscriptionResponseTest() throws JsonProcessingException {
        SubscriptionEndpoint endpoint = new SubscriptionEndpoint(
                "my-host",
                5671,
                "my-source",
                0,
                0
        );

        Subscription subscription = new Subscription(
                1,
                SubscriptionStatus.CREATED,
                "messageType = 'DENM'",
                new HashSet<>(Collections.singleton(endpoint)),
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void napSubscriptionCapabilityResponse() throws JsonProcessingException {
        Capability capability = new Capability(
                new DenmApplicationApi(
                        "ID0001,",
                        "ID0001:0001",
                        "NO",
                        "DENM:001",
                        Collections.singleton("123123"),
                        new HashSet<>(Arrays.asList(1,2,3))
                ),
                new MetadataApi()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(capability)));
    }

}
