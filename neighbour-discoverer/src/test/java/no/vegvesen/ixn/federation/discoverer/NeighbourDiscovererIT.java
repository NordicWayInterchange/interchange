package no.vegvesen.ixn.federation.discoverer;


import no.vegvesen.ixn.federation.repository.PostgresTestcontainerInitializer;
import org.apache.http.client.HttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourDiscovererIT {

	@MockBean
	SSLContext mockedSSL;

	@MockBean
	HttpClient mockedHttpClient;

	@Autowired
	NeighbourDiscoverer discoverer;

	@Test
	public void discovererIsAutowired() {
		discoverer.pollSubscriptions();
	}
}
