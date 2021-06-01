package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
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

    @Test
    public void createValidJsonWithBrokers() throws JsonProcessingException {
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                "1",
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                null,
                null,
                false,
                "neighbour1"
        );
        BrokerApi broker1 = new BrokerApi("client1queue", "amqps://a.c-its-interchange.eu:5671");
        BrokerApi broker2 = new BrokerApi("client2queue", "amqps://b.c-its-interchange.eu:5671");

        responseApi.setBrokers(Sets.newLinkedHashSet(broker1, broker2));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseBrokersToObject() throws JsonProcessingException {
        String input = "{\"id\":\"1\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"createNewQueue\":false,\"queueConsumerUser\":\"neighbour1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"messageBrokerUrl\":null,\"queueName\":null,\"brokers\":[{\"queueName\":\"client1queue\",\"messageBrokerUrl\":\"amqps://a.c-its-interchange.eu:5671\",\"maxBandwidth\":null,\"maxMessageRate\":null},{\"queueName\":\"client2queue\",\"messageBrokerUrl\":\"amqps://b.c-its-interchange.eu:5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();

        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(result.toString());
    }
}
