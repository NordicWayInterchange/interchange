package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ExchangeWithDdlQueueTest extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(no.vegvesen.ixn.federation.BiQpidStructureIT.class), "my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    SSLContext sslContext;

    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    );

    @BeforeEach
    public void setUp() {
        sslContext = sslClientContext(stores, "routing_configurer");
        QpidClientConfig config = new QpidClientConfig(sslContext);
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(), qpidContainer.getvHostName(), config.qpidRestTemplate());
    }

    @Test
    public void testSettingDdlQueuetoExchange() throws Exception {
        String queueName = "dlqueue";

        Source source = new Source(qpidContainer.getAmqpsUrl(), queueName, sslContext);
        source.start();

        Exchange exchange = new Exchange("test-exchange", "direct", new AlternateBinding(queueName));

        assertThat(exchange.getName()).isEqualTo("test-exchange");
        assertThat(exchange.getAlternateBinding()).isNotNull();
        assertThat(exchange.getAlternateBinding().destination()).isEqualTo(queueName);
    }

    @Test
    public void testExchangeWithoutDdlQueue() throws Exception {
        String queueName = "dlqueue";

        Source source = new Source(qpidContainer.getAmqpsUrl(), queueName, sslContext);
        source.start();

        Exchange exchange = new Exchange("test-exchange", "direct", null);

        assertThat(exchange.getName()).isEqualTo("test-exchange");
        assertThat(exchange.getAlternateBinding()).isNull();
    }

}

