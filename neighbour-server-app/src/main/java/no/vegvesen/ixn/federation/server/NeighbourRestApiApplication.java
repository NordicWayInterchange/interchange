package no.vegvesen.ixn.federation.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn.federation")
@EnableJpaRepositories(basePackages={"no.vegvesen.ixn.federation.repository"})
@EntityScan(basePackages={"no.vegvesen.ixn.federation.model"})
public class NeighbourRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeighbourRestApiApplication.class, args);
	}

}

