package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestedSubscriptionResponseApiTest {

    @Test
    public void testUnknownFieldsInJson() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        RequestedSubscriptionResponseApi example = new RequestedSubscriptionResponseApi(uuid,"originatingCountry = 'NO'","/1",SubscriptionStatusApi.CREATED);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(example));
        String input = "{\"selector\":\"originatingCountry = 'NO'\",\"foo\":\"bar\",\"path\":\"/1\",\"status\":\"CREATED\"}";
        RequestedSubscriptionResponseApi result = mapper.readValue(input,RequestedSubscriptionResponseApi.class);
        assertThat(result.getSelector()).isEqualTo("originatingCountry = 'NO'");
        assertThat(result.getPath()).isEqualTo("/1");


    }

}
