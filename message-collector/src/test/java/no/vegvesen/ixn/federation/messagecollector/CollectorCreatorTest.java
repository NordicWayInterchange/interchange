package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.ssl.TestSSLContextConfig;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = {CollectorCreator.class, CollectorProperties.class, TestSSLContextConfig.class, TestSSLProperties.class})
class CollectorCreatorTest {

	@Autowired
	CollectorCreator collectorCreator;

	@Autowired
	CollectorProperties collectorProperties;

	@Test
	void collectorCreatorIsAutowired() {
		assertThat(collectorCreator).isNotNull();
		assertThat(collectorProperties.getWritequeue()).isEqualTo("thisQueueIsForPropertyScanTestingOnly");
	}
}