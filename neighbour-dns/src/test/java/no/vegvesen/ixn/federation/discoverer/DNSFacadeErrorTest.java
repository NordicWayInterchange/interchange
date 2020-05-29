package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
@ActiveProfiles("error")
public class DNSFacadeErrorTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testGetNeighbourFailsWithWrongConfiguration(){
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		assertThat(neighbours).isEmpty();
	}
}
