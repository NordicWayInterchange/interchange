package no.vegvesen.ixn.federation.discoverer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class NeighbourDiscovererIT {

	// TODO: WORK IN PROGRESS
	// Used in manual testing of the client-server interaction.

	@Autowired
	NeighbourDiscoverer neighbourDiscoverer;


	@Test
	public void verifyCorrectRepresentationInRemoteDatabase(){

		neighbourDiscoverer.checkForNewInterchanges();

		neighbourDiscoverer.capabilityExchange();

		neighbourDiscoverer.subscriptionRequest();


		try {
			Thread.sleep(240000); // Four minutes.
		}catch (InterruptedException ignore){
		}
	}

}
