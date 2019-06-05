package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class DNSFacadeTest {

	@Mock
	private DNSProperties dnsProperties;

	@InjectMocks
	private DNSFacade dnsFacade;

	@Test
	public void testNumberOfNeighbours(){

		when(dnsProperties.getDomainName()).thenReturn("itsinterchange.eu");
		when(dnsProperties.getControlChannelPort()).thenReturn("8090");


		// FIXME: how to check that this is correct when the number of neighbours in the network increases?
		int expectedNumberOfNeighbours = 2;
		List<Interchange> interchanges = dnsFacade.getNeighbours();
		Assert.assertEquals(expectedNumberOfNeighbours, interchanges.size());
	}

	@Test
	public void testThatDiscoveredNeighboursAreNotNull(){
		List<Interchange> interchanges = dnsFacade.getNeighbours();

		for(Interchange i : interchanges){
			Assert.assertNotNull(i);
		}
	}

}
