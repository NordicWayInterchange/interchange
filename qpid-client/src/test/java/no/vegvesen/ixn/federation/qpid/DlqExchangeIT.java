package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class DlqExchangeIT extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(no.vegvesen.ixn.federation.BiQpidStructureIT.class), "my_ca", HOST_NAME, "routing_configurer");

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
        System.out.println(qpidContainer.getHttpUrl());
    }

    @Test
    public void testSettingDlQueuetoExchange() {
        String queueName = "dlqueue";

        String name = "exchange-with-dlqueue";

        Exchange exchange = qpidClient.createHeadersExchangeWithDlq(name, queueName);


        assertThat(exchange.getName()).isEqualTo(name);
        assertThat(exchange.getAlternateBinding()).isNotNull();
        assertThat(exchange.getAlternateBinding().destination()).isEqualTo(queueName);
    }

    @Test
    public void testExchangeWithoutDlQueue() {
        Exchange exchange = qpidClient.createHeadersExchange("exchange-without-dlqueue");

        assertThat(exchange.getName()).isEqualTo("exchange-without-dlqueue");
        assertThat(exchange.getAlternateBinding()).isNull();
    }

    @Test
    public void testSetupBothExchangeAndDlq()  {
        UUID uuid =  UUID.randomUUID();

        Queue queue = qpidClient.createQueue("dlq-" + uuid);
        Exchange exchange = qpidClient.createHeadersExchangeWithDlq("del-" + uuid, queue.getName());

        assertThat(exchange).isNotNull();
        assertThat(exchange.getAlternateBinding().destination()).isEqualTo(queue.getName());


    }

}

