package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageForwarderIT {

    @Autowired
    MessageForwarder forwarder;
    @Test
    public void testFoo() throws JMSException, NamingException {
        forwarder.setupConnectionsToNewNeighbours();
    }

}
