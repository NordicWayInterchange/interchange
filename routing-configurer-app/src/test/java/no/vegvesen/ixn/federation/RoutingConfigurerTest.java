package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableScheduling
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
class RoutingConfigurerTest {

	@MockBean
	RestTemplate restTemplate;

	@MockBean
	NeighbourService neighbourService;

	@MockBean
	ServiceProviderRouter serviceProviderRouter;

	@MockBean
	SelfService selfService;

	@Autowired
	RoutingConfigurer routingConfigurer;

	@Autowired
	RoutingConfigurerProperties routingConfigurerProperties;

	@Test
	void routingConfigurerIsAutowired() {
		assertThat(routingConfigurer).isNotNull();
	}

	@Test
	void routingConfigurerPropertyFromMainIsRead() {
		assertThat(routingConfigurerProperties.getInterval()).isNotNull().isEqualTo(10000);
	}

}