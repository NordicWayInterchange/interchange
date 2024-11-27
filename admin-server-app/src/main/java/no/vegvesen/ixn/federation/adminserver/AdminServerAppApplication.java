package no.vegvesen.ixn.federation.adminserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class AdminServerAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServerAppApplication.class, args);
    }

}
