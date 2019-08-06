package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DNSFacade.class, DNSProperties.class})
@EnableConfigurationProperties
@ActiveProfiles("error")
public class DNSFacadeErrorTest {

	@Autowired
	private DNSFacade dnsFacade;

	@Test
	public void testGetNeighbourFailsWithWrongConfiguration(){
		List<Interchange> neighbours = dnsFacade.getNeighbours();
		assertThat(neighbours).isEmpty();
	}
}
