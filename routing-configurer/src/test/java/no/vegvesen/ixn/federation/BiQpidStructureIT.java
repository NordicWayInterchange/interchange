package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class BiQpidStructureIT extends QpidDockerBaseIT {

    private static Logger logger = LoggerFactory.getLogger(BiQpidStructureIT.class);

    private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

    @Container
    private static KeysContainer keyContainer = getKeyContainer(testKeysPath, "my_ca", "localhost", "routing_configurer", "king_gustaf");

    SSLContext sslContext;

    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer("bi-qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password", "localhost")
            .dependsOn(keyContainer);


    @BeforeEach
    public void setUp() {
        KeystoreDetails trust = new KeystoreDetails(
                keyContainer.getKeyFolderOnHost().resolve("truststore.jks").toString(),
                "password",
                KeystoreType.JKS
        );
        KeystoreDetails keys = new KeystoreDetails(
                keyContainer.getKeyFolderOnHost().resolve("routing_configurer.p12").toString(),
                "password",
                KeystoreType.PKCS12,
                "password"
        );
        sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keys,trust);
        QpidClientConfig config = new QpidClientConfig(sslContext);
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate());
    }

    @Test
    public void messageGoesThroughWithOkTTL() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,sslContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "5", 3000));

        Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext);
        MessageConsumer testConsumer = sink.createConsumer();
        Optional<Message> receive = Optional.ofNullable(testConsumer.receive(1000));

        assertThat(receive).isPresent();
    }

    /*
    Testing message inherits TTL from queue when queue TTL is shorter than message TTL
     */
    @Test
    public void messageInheritsTTLFromQueue() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,sslContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "5", 10000));

        Thread.sleep(6000);

        Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext);
        MessageConsumer testConsumer = sink.createConsumer();
        Optional<Message> receive = Optional.ofNullable(testConsumer.receive(1000));

        assertThat(receive).isNotPresent();
    }

    /*
    Testing message does not inherit TTL from queue when queue TTL is longer than message TTL
     */
    @Test
    public void messageDoesNotInheritTTLFromQueue() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,sslContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "5", 3000));

        Thread.sleep(4000);

        Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext);
        MessageConsumer testConsumer = sink.createConsumer();
        Optional<Message> receive = Optional.ofNullable(testConsumer.receive(1000));

        assertThat(receive).isNotPresent();
    }

    private JmsMessage createDenmMessage(Source source, byte[] bytemessage, String causeCode, long ttl) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .causeCode(causeCode)
                .subCauseCode("76")
                .ttl(ttl)
                .build();
    }
}