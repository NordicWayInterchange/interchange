package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= {MockDNSFacade.class, DNSFacade.class})
public class MockDNSFacadeTest {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	DNSFacadeInterface dnsFacade;

	@Test
	public void instanceIsMock() {
		assertThat(dnsFacade).isInstanceOf(MockDNSFacade.class);
	}

	@Test
	public void getNeighbours() {
		List<Interchange> neighbours = dnsFacade.getNeighbours();
		AssertionsForInterfaceTypes.assertThat(neighbours).hasSize(2);
	}
}