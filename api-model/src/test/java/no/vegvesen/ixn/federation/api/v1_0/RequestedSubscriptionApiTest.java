package no.vegvesen.ixn.federation.api.v1_0;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestedSubscriptionApiTest {

    @Test
    public void testUnknownFieldInJson() throws JacksonException {
        ObjectMapper mapper = new ObjectMapper();
        String example = "{\"selector\":\"messageType = 'DENM'\",\"foo\":\"bar\"}";
        RequestedSubscriptionApi result = mapper.readValue(example,RequestedSubscriptionApi.class);
        assertThat(result.getConsumerCommonName()).isNull();

    }
}
