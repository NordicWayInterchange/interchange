package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.LocalDeliveryEndpoint;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class QpidServiceIT extends QpidDockerBaseIT {

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

    private AdminQpidClient client;

    private QpidService service;

    @Mock
    private OutgoingMatchRepository outgoingMatchRepository;

    @BeforeEach
    public void setupClient() {
        SSLContext sslContext = sslClientContext(stores, CLIENT_USER);
        client = new AdminQpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),createRestTemplate(sslContext));
        service = new QpidService(client, outgoingMatchRepository);
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

        System.out.println(selector);

        LocalDelivery aDelivery = new LocalDelivery();
        aDelivery.setSelector(selector);
        aDelivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, "my-exchange"));
        String deliveryUui = aDelivery.getUuid();


        ServiceProvider aServiceProvider = new ServiceProvider("actorCommonName");
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(capability), null));
        aServiceProvider.addDeliveries(new HashSet<>(List.of(aDelivery)));

        client.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));
        assertThat(service.bindingExists(exchangeName, queueName)).isTrue();

        assertThat(service.deliverysExchangeBindingToMatchingCapability(aServiceProvider, deliveryUui)).isNotNull();
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

    @Test
    public void testDeliverysExchangeBindingToMatchingCapability() {
        String exchangeName = "my-exchange";
        String selector = "originatingCountry='NO'";
        client.createHeadersExchange(exchangeName);

        String actorCommonName = "actor-1";

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

        LocalDelivery aDelivery = new LocalDelivery();
        aDelivery.setSelector(selector);
        String deliveryUui = aDelivery.getUuid();

        System.out.println(deliveryUui);

        ServiceProvider aServiceProvider = new ServiceProvider(actorCommonName);
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(capability), null));
        aServiceProvider.addDeliveries(new HashSet<>(List.of(aDelivery)));

        CapabilityApi response1 = service.deliverysExchangeBindingToMatchingCapability(aServiceProvider, deliveryUui);

        assertThat(response1).isNotNull();
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
