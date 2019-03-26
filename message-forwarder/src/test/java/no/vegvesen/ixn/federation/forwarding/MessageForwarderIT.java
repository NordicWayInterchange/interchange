package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageForwarderIT {

    @Autowired
    NeighbourFetcher fetcher;

    @Test
    public void testFoo() {
        List<Interchange> interchanges = fetcher.listNeighbourCandidates();
        for (Interchange i : interchanges) {
            System.out.println(i.getName() + " " + i.getControlChannelPort() + " " + i.getDomainName() + " " + i.getSubscriptions());
        }
    }

}
