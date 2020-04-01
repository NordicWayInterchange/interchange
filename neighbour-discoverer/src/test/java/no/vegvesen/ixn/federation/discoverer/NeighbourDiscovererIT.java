package no.vegvesen.ixn.federation.discoverer;


import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.apache.http.client.HttpClient;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourDiscovererIT {

	@MockBean
	SSLContext mockedSSL;

	@MockBean
	HttpClient mockedHttpClient;

	@MockBean
	DNSFacade mockDnsFacade;

	@MockBean
	NeighbourRESTFacade mockNeighbourRESTFacade;

	@Autowired
	SelfRepository selfRepository;

	@Autowired
	NeighbourDiscoverer discoverer;

	@Autowired
	NeighbourRepository repository;

	@Value("${interchange.node-provider.name}")
	String nodeName;

	@Test
	public void discovererIsAutowired() {
		assertThat(discoverer).isNotNull();
	}

	@Test
	public void newUnknownNeighbourIsPickedUpInCapabilityExchange() {
		mockNeighbours("neighbour-one", "neighbour-two");
		discoverer.checkForNewNeighbours();
		selfRepository.save(new Self(nodeName));
		when(mockNeighbourRESTFacade.postCapabilitiesToCapabilities(any(), any())).thenReturn(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet()));
		discoverer.performCapabilityExchangeWithNeighbours();
		verify(mockNeighbourRESTFacade, times(2)).postCapabilitiesToCapabilities(any(), any());
	}

	private void mockNeighbours(String n1, String n2) {
		Neighbour neighbour1 = new Neighbour();
		neighbour1.setName(n1);
		Neighbour neighbour2 = new Neighbour();
		neighbour2.setName(n2);
		when(mockDnsFacade.getNeighbours()).thenReturn(Lists.list(neighbour1, neighbour2));
	}
}
