package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class OnboardRestControllerIT {

    @ClassRule
    public static GenericContainer databaseContainer = new GenericContainer("postgres:9.6")
            .withEnv("POSTGRES_USER","federation")
            .withEnv("POSTGRES_PASSWORD","federation")
            .withEnv("POSTGRES_DB","federation")
            .withExposedPorts(5432);

                                    //se https://www.baeldung.com/spring-boot-testcontainers-integration-test

    @Autowired
    private SelfRepository selfRepository;

    @Test
    public void foo() {

        System.out.println("Wonder if my container has been created!");
        Self self = selfRepository.findByName("bouvet");
        assertThat(self).isNotNull();
    }

}
