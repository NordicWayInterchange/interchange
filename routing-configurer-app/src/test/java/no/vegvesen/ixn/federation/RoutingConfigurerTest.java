package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {RoutingConfigurer.class, QpidClient.class})
@EnableScheduling
class RoutingConfigurerTest {

	@MockBean
	RestTemplate restTemplate;

	@MockBean
	NeighbourRepository neighbourRepository;

	@MockBean
	ServiceProviderRouter serviceProviderRouter;

	@Autowired
	RoutingConfigurer routingConfigurer;

	@Test
	void routingConfigurerIsAutowired() {
		assertThat(routingConfigurer).isNotNull();
	}

}