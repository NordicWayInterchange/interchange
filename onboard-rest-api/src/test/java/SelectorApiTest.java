import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectorApiTest {


    @Test
    public void testGenerateSelectorApi() throws JsonProcessingException {
        SelectorApi api = new SelectorApi("countryCode = 'NO' and messageType = 'DENM'");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));
    }


    @Test
    public void addSubscriptionsResponse() throws JsonProcessingException {
        LocalSubscriptionApi api = new LocalSubscriptionApi(
                1,
                LocalSubscriptionStatusApi.REQUESTED,
                "countryCode = 'NO' and messageType = 'DENM'",
                Boolean.FALSE,
                Collections.singleton(new LocalBrokerApi(
                        "myQueue",
                        "amqps://myserver",
                        0,
                        0
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void listSubscriptionsResponse() throws JsonProcessingException {
        LocalSubscriptionListApi api = new LocalSubscriptionListApi(Arrays.asList(
                new LocalSubscriptionApi(
                        1,
                        LocalSubscriptionStatusApi.CREATED,
                        "countryCode = 'NO' and messageType = 'DENM'",
                        Boolean.FALSE,
                        Collections.singleton(new LocalBrokerApi(
                                "myQueue",
                                "amqps://myserver",
                                0,
                                0)
                        )
                )
        ));
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void createDENMCapability() throws JsonProcessingException {
        DenmCapabilityApi api = new DenmCapabilityApi(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("6")

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void localCapabilityApi() throws JsonProcessingException {
        LocalCapability api = new LocalCapability(
                1,
                new DenmCapabilityApi(
                        "NPRA",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void localCapabilityList() throws JsonProcessingException {
        LocalCapabilityList api = new LocalCapabilityList(
                Arrays.asList(
                        new LocalCapability(
                                1,
                                new DenmCapabilityApi(
                                        "NPRA",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton("6")
                                )
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));
    }
}
