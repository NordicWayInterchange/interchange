package no.vegvesen.ixn.napcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"no.vegvesen.ixn.federation", "no.vegvesen.ixn.serviceprovider", "no.vegvesen.ixn.napcore","no.vegvesen.ixn.federation.repository"})
public class NapCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NapCoreApplication.class, args);
    }
}
