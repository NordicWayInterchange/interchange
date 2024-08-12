package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionPollResponseApiTest {

    @Test
    public void createValidJson() throws JsonProcessingException {
        EndpointApi endpoint = new EndpointApi("client1queue","b.c-its-interchange.eu", 5671);
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                UUID.randomUUID().toString(),
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
        String input = "{\"foo\":\"bar\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"client1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"endpoints\":[{\"source\":\"client1source\",\"host\":\"b.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();
        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(mapper.writeValueAsString(result));
        assertThat(result.getConsumerCommonName()).isEqualTo("client1");
        assertThat(result.getEndpoints().stream().findFirst().get().getHost()).isEqualTo("b.c-its-interchange.eu");
    }

    @Test
    public void createValidJsonWithEndpoints() throws JsonProcessingException {
        EndpointApi endpoint1 = new EndpointApi("client1source", "a.c-its-interchange.eu", 5671);
        EndpointApi endpoint2 = new EndpointApi("client2source", "b.c-its-interchange.eu", 5671);
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApi(
                UUID.randomUUID().toString(),
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
        String input = "{\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"neighbour1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"lastUpdatedTimestamp\":null,\"endpoints\":[{\"source\":\"client1source\",\"host\":\"a.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null},{\"source\":\"client2queue\",\"host\":\"b.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();

        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }
}
