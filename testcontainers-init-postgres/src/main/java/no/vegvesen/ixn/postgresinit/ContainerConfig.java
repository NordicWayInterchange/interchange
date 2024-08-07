package no.vegvesen.ixn.postgresinit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class ContainerConfig {

    static final String FEDERATION = "federation";

    @Bean
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer(){
        return new PostgreSQLContainer(("postgres:15"))
                .withUsername(FEDERATION)
                .withPassword(FEDERATION)
                .withDatabaseName(FEDERATION);
    }

}
