package no.vegvesen.ixn.federation.service.routing.localdelivery;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

@Testcontainers
@SpringBootTest
public class DatabaseAndQpidIT {


    public static final String HOST_NAME = QpidDockerBaseIT.getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = QpidDockerBaseIT.generateStores(QpidDockerBaseIT.getTargetFolderPathForTestClass(DatabaseAndQpidIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.1")
            .withDatabaseName("federation")
            .withUsername("federation")
            .withPassword("federation");

    @Container
    public static QpidContainer qpidContainer = QpidDockerBaseIT.getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    );


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
        registry.add("routing-configurer.interval",()->"999");
        registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
        registry.add("routing-configurer.vhost",() -> HOST_NAME);
    }

    @Autowired
    RoutingConfigurerProperties routingConfigurerProperties;

    @Autowired
    private OutgoingMatchRepository outgoingMatchRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private LocalDeliveryService localDeliveryService;

    private QpidClient qpidClient ;

    @BeforeEach
    public void setup() {
        qpidClient = new QpidClient(
                new QpidClientConfig(
                        QpidDockerBaseIT.sslClientContext(stores,"routing_configurer")
                ).qpidRestTemplate(),
                routingConfigurerProperties
        );
    }



    @Test
    public void test() {
        QpidDelta qpidDelta = qpidClient.getQpidDelta();
    }

}
