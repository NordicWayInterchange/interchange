package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.SubscriptionMatchDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {MessageCollector.class, CollectorCreator.class, CollectorProperties.class, InterchangeNodeProperties.class, GracefulBackoffProperties.class})
@EnableScheduling //Enable scheduling to verify scheduling parameters annotated with ${...}
class MessageCollectorTestSpring {

	@MockBean
	SSLContext sslContext;

	@MockBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockBean
	SubscriptionMatchDiscoveryService subscriptionMatchDiscoveryService;

	@Autowired
	MessageCollector messageCollector;

	@Autowired
	CollectorProperties collectorProperties;

	@Test
	void collectorCreatorPropertiesIsReadFromPropertiesFile() {
		assertThat(collectorProperties.getWritequeue()).isEqualTo("thisQueueIsForPropertyScanTestingOnly");
		assertThat(collectorProperties.getFixeddelay()).isEqualTo("3000");
	}

	@Test
	void collectorCreatorPropertiesDefaultValueIsPopulated() {
		assertThat(collectorProperties.getLocalIxnFederationPort()).isEqualTo("5671");
	}

	@Test
	void collectorAutowired() {
		assertThat(messageCollector).isNotNull();
	}
}