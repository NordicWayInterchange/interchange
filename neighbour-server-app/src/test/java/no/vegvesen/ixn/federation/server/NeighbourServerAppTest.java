package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourServerAppTest {

	@Autowired
	NeighbourService neighbourService;

	@Autowired
	private NeighbourRestController neighbourRestController;

	@Test
	void neighbourServiceIsAutowired() {
		assertThat(neighbourService).isNotNull();
		assertThat(neighbourRestController).isNotNull();
	}
}
