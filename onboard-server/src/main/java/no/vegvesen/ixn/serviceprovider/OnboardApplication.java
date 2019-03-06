package no.vegvesen.ixn.serviceprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories({"no.vegvesen.ixn.serviceprovider.repository"})
@EntityScan({"no.vegvesen.ixn.federation.model", "no.vegvesen.ixn.serviceprovider.model"})
public class OnboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnboardApplication.class, args);
	}

}

