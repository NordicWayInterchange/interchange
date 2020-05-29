package no.vegvesen.ixn.federation.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn.federation")
public class NeighbourRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeighbourRestApiApplication.class, args);
	}

}

