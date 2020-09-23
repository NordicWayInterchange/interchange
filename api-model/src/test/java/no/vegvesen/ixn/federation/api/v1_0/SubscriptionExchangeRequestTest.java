package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SubscriptionExchangeRequestTest {

    @Test
    public void subsctiptionRequestOneSingleSubscriptionWithOnlySelector() throws JsonProcessingException {
        SubscriptionExchangeSubscriptionRequestApi subscription = new SubscriptionExchangeSubscriptionRequestApi(
                "messageType='DENM' AND originatingCountry='SE'");
        SubscriptionExchangeRequestApi requestApi = new SubscriptionExchangeRequestApi("a.c-itsi-interchange.eu",
                Collections.singleton(subscription));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));
    }

    @Test
    public void subscriptionRequesWithBothCreateQueueAndWithout() throws JsonProcessingException {
        SubscriptionExchangeSubscriptionRequestApi sub1 = new SubscriptionExchangeSubscriptionRequestApi(
                "messageType='DENM' AND orginatingCountry='SE'"
        );
        SubscriptionExchangeSubscriptionRequestApi sub2 = new SubscriptionExchangeSubscriptionRequestApi(
                "messageType='DENM' AND originatingCountry='NO'",
                true,
                "client1"
        );
        SubscriptionExchangeRequestApi requestApi = new SubscriptionExchangeRequestApi("a.c-its-interchange.eu",
                new HashSet<>(Arrays.asList(sub1,sub2)));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));


    }
}
