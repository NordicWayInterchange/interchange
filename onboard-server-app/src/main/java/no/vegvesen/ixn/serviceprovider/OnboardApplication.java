package no.vegvesen.ixn.serviceprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"no.vegvesen.ixn.federation", "no.vegvesen.ixn.serviceprovider"})
public class OnboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnboardApplication.class, args);
	}

}

