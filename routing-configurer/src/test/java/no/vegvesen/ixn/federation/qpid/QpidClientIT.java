package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class})
@RunWith(SpringRunner.class)
public class QpidClientIT {

	@Autowired
	QpidClient client;

	@Test
	public void pingQpid() {
		assertThat(client.ping()).isEqualTo(200);
	}

	@Test
	public void createQueue() throws Exception {
		Interchange findus = new Interchange("findus", Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), "localhost", "8080");
		client.createQueue(findus);
	}

	@Test(expected = Exception.class)
	public void createQueueWithIllegalCharactersInIdFails() throws Exception {
		Interchange torsk = new Interchange("torsk", Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), "localhost", "8080");
		client.createQueue(torsk);
		client.createQueue(torsk); //create some queue that already exists
	}
}