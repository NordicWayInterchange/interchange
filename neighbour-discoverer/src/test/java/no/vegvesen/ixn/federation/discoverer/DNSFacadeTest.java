package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;


/**
 * Depends on DNS Java Record
 */

public class DNSFacadeTest {

	private DNSFacade dnsFacade = new DNSFacade(".itsinterchange.eu", "8080", "5672");
	private List<Interchange> neighbours = new ArrayList<>();

	@Test
	public void testNumberOfNeighbours(){

		int expectedNumberOfRecords = 2;
		List<Interchange> interchanges = dnsFacade.getNeighbours();
		Assert.assertEquals(expectedNumberOfRecords, interchanges.size());
	}

	@Test
	public void testThatDiscoveredNeighborusAreNotNull(){
		List<Interchange> interchanges = dnsFacade.getNeighbours();

		for(Interchange i : interchanges){
			Assert.assertNotNull(i);
		}
	}

	@Test
	public void checkDomainIsRight(){
		String expectedDomain = ".itsinterchange.eu";

		Assert.assertEquals(expectedDomain, dnsFacade.getDomain());
	}
}
