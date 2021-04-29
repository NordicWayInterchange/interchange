package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestedSubscriptionApiTest {

    @Test
    public void testUnknownFieldInJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String example = "{\"selector\":\"messageType = 'DENM'\",\"createNewQueue\":true,\"foo\":\"bar\"}";
        RequestedSubscriptionApi result = mapper.readValue(example,RequestedSubscriptionApi.class);
        assertThat(result.getCreateNewQueue()).isTrue();
        assertThat(result.getQueueConsumerUser()).isNull();

    }
}
