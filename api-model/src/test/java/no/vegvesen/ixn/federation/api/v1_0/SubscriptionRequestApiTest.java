package no.vegvesen.ixn.federation.api.v1_0;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SubscriptionRequestApiTest {

    @Test
    public void subscriptionRequestOneSingleSubscriptionWithOnlySelector() throws JacksonException {
        RequestedSubscriptionApi subscription = new RequestedSubscriptionApi(
                "messageType='DENM' AND originatingCountry='SE'");
        SubscriptionRequestApi requestApi = new SubscriptionRequestApi("client1",
                Collections.singleton(subscription));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));
    }

    @Test
    public void subscriptionRequestWithBothConsumerCommonNameAsClientNameAndIxnName() throws JacksonException {
        RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi(
                "messageType='DENM' AND orginatingCountry='SE'"
        );
        RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi(
                "messageType='DENM' AND originatingCountry='NO'",
                "client1"
        );
        SubscriptionRequestApi requestApi = new SubscriptionRequestApi("a.c-its-interchange.eu",
                new HashSet<>(Arrays.asList(sub1,sub2)));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestApi));
    }

    @Test
    public void testUnknownJsonFields() throws JacksonException {
        SubscriptionRequestApi example = new SubscriptionRequestApi("test",new HashSet<>());
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(example));
        String input = "{\"version\":\"1.0\",\"foo\":\"bar\",\"name\":\"test\",\"subscriptions\":[]}";
        SubscriptionRequestApi result = mapper.readValue(input,SubscriptionRequestApi.class);

    }
}
