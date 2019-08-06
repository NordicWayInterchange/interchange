package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
public class DNSFacadeTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testNumberOfNeighbours(){
		List<Interchange> interchanges = dnsFacade.getNeighbours();
		Assert.assertTrue("Number of known interchanges in the actual dns is less than two", interchanges.size() >= 2);
	}

	@Test
	public void testThatDiscoveredNeighboursAreNotNull(){
		List<Interchange> interchanges = dnsFacade.getNeighbours();

		for(Interchange i : interchanges){
			System.out.println(i.toString());
			Assert.assertNotNull(i);
		}
	}

	@Test
	public void ericssonPresent() {
		Interchange ericsson = null;
		for (Interchange neighbour : dnsFacade.getNeighbours()) {
			if (neighbour.getName().equals("ericsson.itsinterchange.eu")){
				ericsson = neighbour;
			}
		}
		Assert.assertNotNull(ericsson);
		Assert.assertNotNull(ericsson.getControlChannelUrl("/"));
		Assert.assertNotNull(ericsson.getMessageChannelPort());

	}
}
