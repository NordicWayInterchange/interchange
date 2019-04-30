package no.vegvesen.ixn.federation.discoverer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NeighbourDiscovererIT {

	@Autowired
	NeighbourDiscoverer neighbourDiscoverer;


	@Test
	public void verifyCorrectRepresentationInRemoteDatabase(){

		neighbourDiscoverer.checkForNewInterchanges();

		neighbourDiscoverer.capabilityExchange();

		neighbourDiscoverer.subscriptionRequest();

		//neighbourDiscoverer.pollSubscriptions();

		try {
			Thread.sleep(20000);
		}catch (InterruptedException ignore){
		}
	}

}
