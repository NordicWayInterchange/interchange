package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * NB: Forutsetter en kjørende database - In memory database?
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class NeighbourDiscovererTest {

	// Mocks
	private InterchangeRepository interchangeRepository = mock(InterchangeRepository.class);
	private ServiceProviderRepository serviceProviderRepository = mock(ServiceProviderRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);
	private RestTemplate restTemplate = mock(RestTemplate.class);
	private NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade, interchangeRepository, serviceProviderRepository, restTemplate, "bouvet");
	private Interchange ericsson = new Interchange();
	private List<Interchange> neighbours = new ArrayList<>();

	@Before
	public void before(){
		ericsson.setName("ericsson");
		neighbours.add(ericsson);
	}

	@Test
	public void testNewInterchangeIsAddedToDatabase(){
		// DNS facade returns one interchange from the lookup.
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));

		// Pretending that the interchange does not exist in the database.
		when(interchangeRepository.findByName(any(String.class))).thenReturn(null);

		when(interchangeRepository.save(any(Interchange.class))).thenReturn(any(Interchange.class));

		neighbourDiscoverer.checkForNewInterchanges();

		// verify that the interchange is saved to the database.
		verify(interchangeRepository, times(1)).save(any(Interchange.class));
	}

	@Test
	public void testKnownInterchangeIsNotAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));

		// Pretending interchange 'ericsson' is already in the database(is known to us).
		when(interchangeRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourDiscoverer.checkForNewInterchanges();

		verify(interchangeRepository, times(0)).save(any(Interchange.class));

	}

	@Test
	public void testInterchangeWithSameNameAsUsIsNotSaved(){

		Interchange dnsReturn = neighbourDiscoverer.getRepresentationOfCurrentNode();

		// Mocking finding ourselves in the DNS lookup.
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(dnsReturn));

		neighbourDiscoverer.checkForNewInterchanges();

		verify(interchangeRepository, times(0)).save(any(Interchange.class));
	}


	@Test
	public void testCheckForChangesInCapa(){
		String expectedURL = "ericsson.itsinterchange.eu:8080";

	}



}
