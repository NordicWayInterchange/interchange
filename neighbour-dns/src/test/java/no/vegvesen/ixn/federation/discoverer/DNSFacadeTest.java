package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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
			assertThat(i).isNotNull();
		}
	}

	@Test
	public void bouvetNodePresent() {
		Neighbour bouvet = null;
		for (Neighbour neighbour : dnsFacade.getNeighbours()) {
			if (neighbour.getName().equals("bouveta-fed.itsinterchange.eu")){
				bouvet = neighbour;
			}
		}
		assertThat(bouvet).isNotNull();
		assertThat(bouvet.getControlChannelUrl("/")).isNotNull();
		assertThat(bouvet.getMessageChannelPort()).isNotNull().isEqualTo("5671");
	}

	@Test
	public void findBouvetExists() {
		Neighbour neighbour = dnsFacade.findNeighbour("bouveta-fed.itsinterchange.eu");
		assertThat(neighbour).isNotNull();
	}

	@Test(expected = InterchangeNotInDNSException.class)
	public void findNotDefinedDoesNotExists() {
		dnsFacade.findNeighbour("no-such-interchange.itsinterchange.eu");
	}
}
