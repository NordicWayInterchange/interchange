package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
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
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		Assert.assertTrue("Number of known Neighbours in the actual dns is less than two", neighbours.size() >= 2);
	}

	@Test
	public void testThatDiscoveredNeighboursAreNotNull(){
		List<Neighbour> neighbours = dnsFacade.getNeighbours();

		for(Neighbour i : neighbours){
			System.out.println(i.toString());
			Assert.assertNotNull(i);
		}
	}

	@Test
	public void ericssonPresent() {
		Neighbour ericsson = null;
		for (Neighbour neighbour : dnsFacade.getNeighbours()) {
			if (neighbour.getName().equals("ericsson.itsNeighbour.eu")){
				ericsson = neighbour;
			}
		}
		Assert.assertNotNull(ericsson);
		Assert.assertNotNull(ericsson.getControlChannelUrl("/"));
		Assert.assertNotNull(ericsson.getMessageChannelPort());

	}
}
