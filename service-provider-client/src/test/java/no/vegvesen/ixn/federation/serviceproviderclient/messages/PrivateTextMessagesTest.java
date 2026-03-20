package no.vegvesen.ixn.federation.serviceproviderclient.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessage;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessages;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateTextMessagesTest {

    @Test
    public void createPrivateTextMessages() throws JsonProcessingException {
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
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateTextMessages));
    }

}
