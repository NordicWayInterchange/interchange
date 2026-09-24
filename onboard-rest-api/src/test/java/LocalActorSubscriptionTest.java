import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscriptionStatusApi;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalActorSubscriptionTest {

    @Test
    public void renderedOutputContainsLastUpdatedTimeStamp() {
        LocalActorSubscription localActorSubscription = new LocalActorSubscription(
                "uuid-123",
               "/a/b",
               "a = b",
                "consumer",
                123l,
                LocalActorSubscriptionStatusApi.CREATED,
                null,
                null

        );
        ObjectMapper mapper = JsonMapper.builder().build();
        String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(localActorSubscription);
        assertThat(output).contains("\"lastUpdatedTimeStamp\"");
    }


}
