package no.vegvesen.ixn.federation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@SpringBootConfiguration
public class RoutingConfigurerApplication {

	public static void main(String[] args){
		SpringApplication.run(RoutingConfigurerApplication.class);
	}

}
