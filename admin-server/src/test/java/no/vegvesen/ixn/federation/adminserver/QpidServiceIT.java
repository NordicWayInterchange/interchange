package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private AdminQpidDelta delta;

    private QpidService service;

    private OutgoingMatchRepository outgoingMatchRepository;

    private ServiceProviderRepository serviceProviderRepository;


    @BeforeEach
    public void setupClient() {
        SSLContext sslContext = sslClientContext(stores, CLIENT_USER);
        outgoingMatchRepository = mock(OutgoingMatchRepository.class);
        serviceProviderRepository = mock(ServiceProviderRepository.class);
        client = new AdminQpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),createRestTemplate(sslContext));
        delta = mock(AdminQpidDelta.class);
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

    @Test
    void TestGetDeliverysExchangeBindingToMatchingCapability() {
        String serviceProviderName = "my-service-provider";
        String selector = "originatingCountry = 'NO'";

        Capability capability = new Capability(
                new DenmApplication(
                        "NO12345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        List.of("0123"),
                        List.of(5)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );
        CapabilityShard shard = new CapabilityShard(1, "exchange", "publicationId = 'pub-1'");
        capability.setShards(Collections.singletonList(shard));
        client.createHeadersExchange("exchange");

        assertThat(CapabilityMatcher.matchCapabilitiesToSelector(Collections.singleton(capability), selector)).hasSize(1);

        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, "exchange");
        LocalDelivery delivery = new LocalDelivery(
                1,
                new HashSet<>(Collections.singletonList(endpoint)),
                selector,
                LocalDeliveryStatus.CREATED);

        ServiceProvider aServiceProvider = new ServiceProvider(serviceProviderName);
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(capability), null));
        aServiceProvider.setDeliveries(new HashSet<>(Collections.singleton(delivery)));
        serviceProviderRepository.save(aServiceProvider);

        List<OutgoingMatch> mockMatches = new ArrayList<>();
        mockMatches.add(new OutgoingMatch(delivery, capability, serviceProviderName));

        when(outgoingMatchRepository.findAllByLocalDelivery_Uuid(delivery.getUuid())).thenReturn(mockMatches);
        when(delta.exchangeHasBindingToQueue("exchange", "queue")).thenReturn(true);
        for (CapabilityShard capabilityShard : capability.getShards()) {
            System.out.println(capabilityShard.getExchangeName());
            AssertionsForClassTypes.assertThat(client.exchangeExists(capabilityShard.getExchangeName())).isTrue();
        }

        CapabilityApi response1 = service.deliverysExchangeBindingToMatchingCapability( aServiceProvider, delivery.getUuid());
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
