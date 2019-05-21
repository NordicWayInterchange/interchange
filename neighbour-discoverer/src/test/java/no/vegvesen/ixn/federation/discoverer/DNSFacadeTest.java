package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class DNSFacadeTest {

	private DNSFacade dnsFacade = mock(DNSFacade.class);

	@Before
	public void before(){
		Interchange bouvet = new Interchange();
		bouvet.setName("bouvet");

		Interchange ericsson = new Interchange();
		ericsson.setName("ericsson");

		when(dnsFacade.getNeighbours()).thenReturn(Arrays.asList(bouvet, ericsson));
	}


	@Test
	public void testNumberOfNeighbours(){

		int expectedNumberOfNeighbours = 2;
		List<Interchange> interchanges = dnsFacade.getNeighbours();
		Assert.assertEquals(expectedNumberOfNeighbours, interchanges.size());
	}

	@Test
	public void testThatDiscoveredNeighborusAreNotNull(){
		List<Interchange> interchanges = dnsFacade.getNeighbours();

		for(Interchange i : interchanges){
			Assert.assertNotNull(i);
		}
	}

}
