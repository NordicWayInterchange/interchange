import no.vegvesen.ixn.MessageListApi;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageListApiTest {

    @Test
    public void readMessageList() throws IOException {
        File messageFile = new File("test_messages.json");
        ObjectMapper mapper = new ObjectMapper();
        MessageListApi messages = mapper.readValue(messageFile, MessageListApi.class);

        System.out.println(messages.getMessages().get(0).toString());

        assertThat(messages.getMessages().get(0).getMessageText()).isEqualTo("First message");
    }
}
