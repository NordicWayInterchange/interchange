package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
@SpringBootTest(classes = {
        QpidService.class,
        QpidClient.class,
        QpidClientConfig.class,
        RoutingConfigurerProperties.class,
        SslAutoConfiguration.class
})

 */
@Testcontainers
public class QpidServiceIT extends QpidDockerBaseIT {

    //@Autowired
    //private QpidService service;

    //@Autowired


    public static final String HOST_NAME = getDockerHost();

    private static final String CLIENT_USER = "admin_server";
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(QpidServiceIT.class), "my_ca", HOST_NAME, CLIENT_USER);

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    );

    private QpidClient client;

    @BeforeEach
    public void setupClient() {
        SSLContext sslContext = sslClientContext(stores, CLIENT_USER);
        client = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),createRestTemplate(sslContext));
    }

    /*
    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
        registry.add("routing-configurer.vhost", () -> "localhost");
        registry.add("KEY_STORE", () -> getClientStorePath("admin_server", stores.clientStores()));
        registry.add("TRUST_STORE", () -> getTrustStorePath(stores));
        registry.add("KEY_STORE_PASSWORD", () -> "password");
        registry.add("TRUST_STORE_PASSWORD", () -> "password");
    }

     */
/*
    @BeforeAll
    static void setUp() {
        qpidContainer.start();

    }
    @Test
    public void serviceIsAutowired() {
        assertThat(service).isNotNull();
    }
*/

    @Test
    public void testExchangeExists() {
        client.createDirectExchange("exchange-1");
        assertThat(client.exchangeExists("exchange-1")).isTrue();
    }

    @Test
    public void testQueueExists() {
        client.createQueue("queue-1");
        assertThat(client.queueExists("queue-1")).isTrue();
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
        assertThat(client.getExchange(exchangeName).isBoundToQueue(queueName)).isTrue();
    }

    @Test
    public void testBindingExistsReturnsFalse(){
        String queueName = "bi-queue";
        String exchangeName = "my-exchange-2";

        client.createHeadersExchange(exchangeName);

        assertThat(client.getExchange(exchangeName).isBoundToQueue(queueName)).isFalse();
    }

    @Test
    public void testGetExchanges() throws JsonProcessingException {
        Exchange exchange = client.createDirectExchange("test-exchange");
        assertThat(exchange.getName()).isEqualTo("test-exchange");
        assertThat(client.getAllExchanges()).isNotEmpty();
    }

    @Test
    public void testGetQueues() throws JsonProcessingException {
        Queue queue = client.createQueue("test-queue");
        assertThat(queue.getName()).isEqualTo("test-queue");
        assertThat(client.getAllQueues()).isNotEmpty();
    }

    private RestTemplate createRestTemplate(SSLContext sslContext) {
        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).build();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setSSLSocketFactory(sslConnectionSocketFactory).build();
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }
}
