package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
public class DNSFacadeTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testNumberOfNeighbours(){
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		assertThat(neighbours)
				.withFailMessage("Number of known Neighbours in the actual dns is less than two")
				.hasSizeGreaterThan(1);
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
			if (neighbour.getName().equals("no-fed2.itsinterchange.eu")){
				bouvet = neighbour;
			}
		}
		assertThat(bouvet).isNotNull();
		assertThat(bouvet.getControlChannelUrl("/")).isNotNull();
		assertThat(bouvet.getMessageChannelPort()).isNotNull().isEqualTo("5671");
	}

}
