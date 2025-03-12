package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.testssl.SSLContextConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        QpidService.class,
        QpidClient.class,
        QpidClientConfig.class,
        RoutingConfigurerProperties.class,
        SSLContextConfig.class,
        TestSSLProperties.class
})
public class QpidServiceIT extends QpidDockerBaseIT {

    @Autowired
    private QpidService service;

    @Autowired
    private QpidClient client;

    public static final String HOST_NAME = getDockerHost();

    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(QpidServiceIT.class), "my_ca", HOST_NAME, "admin_server");

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    );

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
        registry.add("routing-configurer.vhost", () -> "localhost");
        registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
        registry.add("test.ssl.key-store", () -> getClientStorePath("admin_server", stores.clientStores()));
    }

    @BeforeAll
    static void setUp() {
        qpidContainer.start();

    }

    @Test
    public void serviceIsAutowired() {
        assertThat(service).isNotNull();
    }

    @Test
    public void testExchangeExists() {
        client.createDirectExchange("exchange-1");
        assertThat(service.exchangeExists("exchange-1")).isTrue();
    }

    @Test
    public void testQueueExists() {
        client.createQueue("queue-1");
        assertThat(service.queueExists("queue-1")).isTrue();
    }

    @Test
    public void testBindingExists() {
        String queueName = "bi-queue";
        String exchangeName = "my-exchange";

        client.createHeadersExchange(exchangeName);

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("12", "13"),
                        List.of(5, 6)
                ),
                new Metadata()
        );
        String selector = MessageValidatingSelectorCreator.makeSelector(capability, null);

        client.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));
        assertThat(service.bindingExists(exchangeName, queueName)).isTrue();
    }

    @Test
    public void testBindingExistsReturnsFalse(){
        String queueName = "bi-queue";
        String exchangeName = "my-exchange-2";

        client.createHeadersExchange(exchangeName);

        assertThat(service.bindingExists(exchangeName, queueName)).isFalse();
    }

    @Test
    public void testGetExchanges() {
        Exchange exchange = client.createDirectExchange("test-exchange");
        assertThat(exchange.getName()).isEqualTo("test-exchange");
        assertThat(service.getAllExchanges()).isNotEmpty();
    }

    @Test
    public void testGetQueues() {
        Queue queue = client.createQueue("test-queue");
        assertThat(queue.getName()).isEqualTo("test-queue");
        assertThat(service.getAllQueues()).isNotEmpty();
    }
}
