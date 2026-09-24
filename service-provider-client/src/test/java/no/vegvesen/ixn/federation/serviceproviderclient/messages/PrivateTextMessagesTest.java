package no.vegvesen.ixn.federation.serviceproviderclient.messages;

import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessage;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessages;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateTextMessagesTest {

    @Test
    public void createPrivateTextMessages() throws JacksonException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("textProperty", "this is text");
        properties.put("booleanProperty", true);
        properties.put("integerProperty", 1);
        PrivateTextMessages privateTextMessages = new PrivateTextMessages(
                List.of(
                        new PrivateTextMessage(
                                "This is a test of private text messages",
                                properties
                        )
                )
        );
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateTextMessages));
    }

}
