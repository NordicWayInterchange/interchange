package no.vegvesen.ixn.federation.api.v1_0;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseApi));
    }
}
