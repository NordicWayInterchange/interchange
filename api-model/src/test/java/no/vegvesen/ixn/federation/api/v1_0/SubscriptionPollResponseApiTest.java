package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionPollResponseApiTest {

    @Test
    public void createValidJson() throws JsonProcessingException {
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                "1",
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "amqps://b.c-its-interchange.eu:5671",
                "client1queue",
                true,
                "client1"
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseUnknownJsonField() throws JsonProcessingException {
        String input = "{\"id\":\"1\",\"foo\":\"bar\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"createNewQueue\":true,\"queueConsumerUser\":\"client1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"messageBrokerUrl\":\"amqps://b.c-its-interchange.eu:5671\",\"queueName\":\"client1queue\"}";
        ObjectMapper mapper = new ObjectMapper();
        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getQueueConsumerUser()).isEqualTo("client1");
    }


}
