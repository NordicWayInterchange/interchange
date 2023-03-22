package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(classes = {ServiceProviderRepository.class})
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class TestServiceProviderRouterDBIT {

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Test
    public void foo() {
        assertThat(serviceProviderRepository).isNotNull();

    }
}
