package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= {MockDNSFacade.class, DNSFacade.class, MockDNSProperties.class, DNSProperties.class})
@EnableConfigurationProperties
public class MockDNSFacadeTest {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	DNSFacadeInterface dnsFacade;

	@Test
	public void instanceIsMock() {
		assertThat(dnsFacade).isInstanceOf(MockDNSFacade.class);
	}

	@Test
	public void dnsMockGetsControlChannelPort() {
		List<Interchange> neighbours = dnsFacade.getNeighbours();
		assertThat(neighbours).hasSize(2);
		Interchange firstNeighbour = neighbours.iterator().next();
		assertThat(firstNeighbour.getControlChannelPort()).isEqualTo("8888");
	}
}