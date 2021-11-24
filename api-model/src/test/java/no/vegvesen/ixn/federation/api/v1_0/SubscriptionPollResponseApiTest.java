package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionPollResponseApiTest {

    @Test
    public void createValidJson() throws JsonProcessingException {
        EndpointApi endpoint = new EndpointApi("client1queue","amqps://b.c-its-interchange.eu:5671");
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                "1",
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "client1",
                Collections.singleton(endpoint)
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseUnknownJsonField() throws JsonProcessingException {
        String input = "{\"id\":\"1\",\"foo\":\"bar\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"client1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"messageBrokerUrl\":\"amqps://b.c-its-interchange.eu:5671\",\"source\":\"client1source\"}";
        ObjectMapper mapper = new ObjectMapper();
        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getConsumerCommonName()).isEqualTo("client1");
    }

    @Test
    public void createValidJsonWithEndpoints() throws JsonProcessingException {
        EndpointApi endpoint1 = new EndpointApi("client1source", "amqps://a.c-its-interchange.eu:5671");
        EndpointApi endpoint2 = new EndpointApi("client2source", "amqps://b.c-its-interchange.eu:5671");
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                "1",
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "neighbour1",
                Sets.newLinkedHashSet(endpoint1,endpoint2)
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseEndpointsToObject() throws JsonProcessingException {
        String input = "{\"id\":\"1\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"queueConsumerUser\":\"neighbour1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"messageBrokerUrl\":null,\"source\":null,\"endpoints\":[{\"queueName\":\"client1source\",\"messageBrokerUrl\":\"amqps://a.c-its-interchange.eu:5671\",\"maxBandwidth\":null,\"maxMessageRate\":null},{\"queueName\":\"client2queue\",\"messageBrokerUrl\":\"amqps://b.c-its-interchange.eu:5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();

        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }
}
