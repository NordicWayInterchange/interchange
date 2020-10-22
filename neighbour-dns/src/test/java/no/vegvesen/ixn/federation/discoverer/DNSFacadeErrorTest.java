package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
@ActiveProfiles("error")
public class DNSFacadeErrorTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testGetNeighbourFailsWithWrongConfiguration(){
		List<Neighbour> neighbours = dnsFacade.lookupNeighbours();
		assertThat(neighbours).isEmpty();
	}
}
