package no.vegvesen.ixn;

import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;

public class BasicSourceSink {

    /*
    Before running this test, to set up manual qpid container managed in the UI:
    1. docker build -t manual_qpid .
    2. docker run -p 8080:8080 -p 5672:5672 manual_qpid
     */
    @Test
    public void sendAndReceiveMessage() throws JMSException, NamingException {
        BasicAuthSource source = new BasicAuthSource("amqp://localhost:5672", "amq.direct", "guest", "guest");

        String messageText = "This is my DENM message :) ";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.start();
        source.sendNonPersistentMessage(source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .originatingCountry("NO")
                .build());
    }
}
