package no.vegvesen.ixn.federation.repository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("no.vegvesen.ixn.federation.repository")
@EntityScan("no.vegvesen.ixn.federation.model")
public class TestApplication {

}