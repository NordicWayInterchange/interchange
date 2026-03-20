package no.vegvesen.ixn.federation.serviceproviderclient.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelRequest;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelRequestApi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class AddPrivateChannelRequestTest {

    @Test
    public void testAddPrivateChannelRequest() throws JsonProcessingException {
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(
                "requestingUser",
                List.of(
                        new PrivateChannelRequestApi(
                                Set.of(
                                        "peer1"
                                ),
                                "test private channel"
                        )
                )

        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

    }
}
