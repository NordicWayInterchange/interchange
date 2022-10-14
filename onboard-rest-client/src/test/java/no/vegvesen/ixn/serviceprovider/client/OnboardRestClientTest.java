package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import no.vegvesen.ixn.serviceprovider.model.AddSubscriptionsRequest;
import no.vegvesen.ixn.serviceprovider.model.SelectorApi;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OnboardRestClientTest {

    @Test
    void testObjectMapperReturnsRightApiObject() throws IOException {
        File file = new File("sub_king_gustaf_denm_no.json");

        ObjectMapper mapper = new ObjectMapper();
        AddSubscriptionsRequest subscriptions = mapper.readValue(file,AddSubscriptionsRequest.class);

        for (AddSubscription subscription : subscriptions.getSubscriptions()) {
            System.out.println(subscription.toString());
        }

        System.out.println();

    }
}
