package no.vegvesen.ixn.serviceprovider;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class LocalhostImportInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url: jdbc:postgresql://localhost:5432/federation",
                "spring.datasource.username: federation",
                "spring.datasource.password: federation",
                "spring.datasource.driver-class-name: org.postgresql.Driver"
        ).applyTo(configurableApplicationContext.getEnvironment());    }
}
