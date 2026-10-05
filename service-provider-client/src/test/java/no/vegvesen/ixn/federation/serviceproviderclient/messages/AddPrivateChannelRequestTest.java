package no.vegvesen.ixn.federation.serviceproviderclient.messages;

import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelRequest;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelRequestApi;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

public class AddPrivateChannelRequestTest {

    @Test
    public void testAddPrivateChannelRequest() throws JacksonException {
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
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

    }
}
