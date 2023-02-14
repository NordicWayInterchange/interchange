package no.vegvesen.ixn.federation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class SubscriptionTest {
    @Test
    public void createAddSubscriptionForTest0() throws IOException {
        AddSubscription subscription = new AddSubscription(
        new SelectorBuilder()
                .originatingCountry("FI")
                .messageType("IVIM")
                .protocolVersion("IVIM:0.0.0")
                .publisherId("SE90009")
                .quadTree(new HashSet<>(Arrays.asList("0","1","2","3")))
                .toSelector()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void createAddSubscriptionForTest1() throws IOException {
        AddSubscription subscription = new AddSubscription(
                new SelectorBuilder()
                        .originatingCountry("FI")
                        .messageType("IVIM")
                        .protocolVersion("IVIM:0.0.1")
                        .publisherId("SE90009")
                        .quadTree(new HashSet<>(Arrays.asList("0","1","2","3")))
                        .toSelector()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void createAddSubscriptionForTest2() throws IOException {
        AddSubscription subscription = new AddSubscription(
                new SelectorBuilder()
                        .originatingCountry("FI")
                        .messageType("IVIM")
                        .protocolVersion("IVIM:0.0.2")
                        .publisherId("SE90009")
                        .quadTree(new HashSet<>(Arrays.asList("0","1","2","3")))
                        .toSelector()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void createAddSubscriptionForTest3() throws IOException {
        AddSubscription subscription = new AddSubscription(
                new SelectorBuilder()
                        .originatingCountry("FI")
                        .messageType("IVIM")
                        .protocolVersion("IVIM:0.0.3")
                        .publisherId("SE90009")
                        .quadTree(new HashSet<>(Arrays.asList("0","1","2","3")))
                        .toSelector()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }
}
