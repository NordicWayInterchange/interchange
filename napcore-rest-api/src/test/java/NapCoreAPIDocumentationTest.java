import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionEndpoint;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

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
                new HashSet<>(Collections.singleton(endpoint)),
                0
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }


}
