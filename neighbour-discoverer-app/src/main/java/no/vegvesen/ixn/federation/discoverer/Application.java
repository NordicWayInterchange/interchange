package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn.federation")
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }
}
