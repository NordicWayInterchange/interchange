package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.subscription.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.subscription.SubscriptionPollResponseApiV1;
import no.vegvesen.ixn.federation.api.v1_0.subscription.SubscriptionPollResponseApiV2;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionPollResponseApiTest {

    @Test
    public void createValidJson() throws JsonProcessingException {
        EndpointApiV1 endpoint = new EndpointApiV1("client1queue","b.c-its-interchange.eu", 5671);
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApiV1(
                UUID.randomUUID().toString(),
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "client1",
                Set.of(endpoint),
                Instant.now().toEpochMilli()
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseUnknownJsonField() throws JsonProcessingException {
        String input = "{\"foo\":\"bar\",\"version\":\"1.2\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"client1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"endpoints\":[{\"source\":\"client1source\",\"host\":\"b.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();
        SubscriptionPollResponseApiV1 result = mapper.readValue(input,SubscriptionPollResponseApiV1.class);
        System.out.println(mapper.writeValueAsString(result));
        assertThat(result.getConsumerCommonName()).isEqualTo("client1");
        assertThat(result.getVersion()).isEqualTo("1.2");
        assertThat(result.getEndpoints().stream().findFirst().orElseThrow().getHost()).isEqualTo("b.c-its-interchange.eu");
    }

    @Test
    public void createValidJsonWithEndpoints() throws JsonProcessingException {
        EndpointApiV1 endpoint1 = new EndpointApiV1("client1source", "a.c-its-interchange.eu", 5671);
        EndpointApiV1 endpoint2 = new EndpointApiV1("client2source", "b.c-its-interchange.eu", 5671);
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApiV1(
                UUID.randomUUID().toString(),
                "messageType='DENM' AND originatingCountry='NO'",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "neighbour1",
                Set.of(endpoint1,endpoint2),
                Instant.now().toEpochMilli()
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(responseApi));
    }

    @Test
    public void parseEndpointsToObject() throws JsonProcessingException {
        String input = "{\"version\":\"1.2\",\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"neighbour1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"lastUpdatedTimestamp\":null,\"endpoints\":[{\"source\":\"client1source\",\"host\":\"a.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null},{\"source\":\"client2queue\",\"host\":\"b.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();

        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Test
    public void marshallAndUnmarshallPollResponseWithDynamicFilter() throws JsonProcessingException {
        String  uuid = UUID.randomUUID().toString();
        SubscriptionPollResponseApi responseApi = new SubscriptionPollResponseApiV2(
                uuid,
                "a = b",
                "/subsctiptions/" +  uuid,
                SubscriptionStatusApi.CREATED,
                "neighboura",
                Set.of(
                        new EndpointApiV2(
                                "source",
                                "host",
                                5671,
                                true
                        )
                ),
                Instant.now().toEpochMilli()
        );
        ObjectMapper mapper = new ObjectMapper();
        String marshalled = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseApi);
        System.out.println(marshalled);

        SubscriptionPollResponseApi result = mapper.readValue(marshalled, SubscriptionPollResponseApi.class);
        System.out.println(result);

    }

    @Test
    public void parseEndpointsToObjectWitMissingVersion() throws JsonProcessingException {
        String input = "{\"selector\":\"messageType='DENM' AND originatingCountry='NO'\",\"consumerCommonName\":\"neighbour1\",\"path\":\"/subscriptions/1\",\"status\":\"CREATED\",\"lastUpdatedTimestamp\":null,\"endpoints\":[{\"source\":\"client1source\",\"host\":\"a.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null},{\"source\":\"client2queue\",\"host\":\"b.c-its-interchange.eu\",\"port\":\"5671\",\"maxBandwidth\":null,\"maxMessageRate\":null}]}";
        ObjectMapper mapper = new ObjectMapper();

        SubscriptionPollResponseApi result = mapper.readValue(input,SubscriptionPollResponseApi.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

}
