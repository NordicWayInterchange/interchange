package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class SubscriptionStatusPollResponseApiTest {

    @Test
    public void createValidJson() throws JsonProcessingException {
        SubscriptionStatusPollResponseApi responseApi = new SubscriptionStatusPollResponseApi(
                "1",
                "messageType='DENM' AND originatingCountry='NO'",
                true,
                "client1",
                "/subscriptions/1",
                SubscriptionStatusApi.CREATED,
                "amqps://b.c-its-interchange.eu:5671",
                "client1queue"
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseApi));
    }
}
