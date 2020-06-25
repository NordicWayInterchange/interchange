package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLContextConfig;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {MessageCollector.class, CollectorCreator.class, CollectorProperties.class, TestSSLContextConfig.class, TestSSLProperties.class})
@EnableScheduling //Enable scheduling to verify scheduling parameters annotated with ${...}
class MessageCollectorTestSpring {

	@MockBean
	NeighbourService neighbourService;

	@Autowired
	MessageCollector messageCollector;

	@Autowired
	CollectorProperties collectorProperties;

	@Test
	void collectorCreatorPropertiesIsReadFromPropertiesFile() {
		assertThat(collectorProperties.getWritequeue()).isEqualTo("thisQueueIsForPropertyScanTestingOnly");
		assertThat(collectorProperties.getFixeddelay()).isEqualTo("30001");
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