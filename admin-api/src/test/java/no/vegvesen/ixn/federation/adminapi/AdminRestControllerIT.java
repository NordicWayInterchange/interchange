package no.vegvesen.ixn.federation.adminapi;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class AdminRestControllerIT {
}
