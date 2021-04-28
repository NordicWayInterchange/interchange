package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionResponseApiTest {

    @Test
    public void testUnknownJsonFields() throws JsonProcessingException {
        SubscriptionResponseApi example = new SubscriptionResponseApi("test",new HashSet<>());
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(example));
        String input = "{\"version\":\"1.0\",\"name\":\"test\",\"foo\":\"bar\",\"subscriptions\":[]}";

        SubscriptionResponseApi result = mapper.readValue(input,SubscriptionResponseApi.class);
        assertThat(result.getName()).isEqualTo("test");

    }

}
