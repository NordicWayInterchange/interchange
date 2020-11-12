package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SubscriptionRequestTest {

    @Test
    public void subscriptionRequestOneSingleSubscriptionWithOnlySelector() throws JsonProcessingException {
        RequestedSubscriptionApi subscription = new RequestedSubscriptionApi(
                "messageType='DENM' AND originatingCountry='SE'");
        SubscriptionRequestApi requestApi = new SubscriptionRequestApi("a.c-itsi-interchange.eu",
                Collections.singleton(subscription));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));
    }

    @Test
    public void subscriptionRequestWithBothCreateQueueAndWithout() throws JsonProcessingException {
        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi(
                "messageType='DENM' AND orginatingCountry='SE'"
        );
        RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi(
                "messageType='DENM' AND originatingCountry='NO'",
                true,
                "client1"
        );
        SubscriptionRequestApi requestApi = new SubscriptionRequestApi("a.c-its-interchange.eu",
                new HashSet<>(Arrays.asList(sub1,sub2)));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));


    }
}
