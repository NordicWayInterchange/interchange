package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestedSubscriptionResponseApiTest {

    @Test
    public void testUnknownFieldsInJson() throws JsonProcessingException {
        RequestedSubscriptionResponseApi example = new RequestedSubscriptionResponseApi("1","originatingCountry = 'NO'","/1",SubscriptionStatusApi.CREATED);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(example));
        String input = "{\"id\":\"1\",\"selector\":\"originatingCountry = 'NO'\",\"foo\":\"bar\",\"path\":\"/1\",\"status\":\"CREATED\"}";
        RequestedSubscriptionResponseApi result = mapper.readValue(input,RequestedSubscriptionResponseApi.class);
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getSelector()).isEqualTo("originatingCountry = 'NO'");
        assertThat(result.getPath()).isEqualTo("/1");


    }

}
