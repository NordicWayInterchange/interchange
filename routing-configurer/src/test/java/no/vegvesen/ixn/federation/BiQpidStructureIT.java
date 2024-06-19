package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class BiQpidStructureIT extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(BiQpidStructureIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    SSLContext sslContext;

    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            Path.of("bi-qpid"),
            stores,
            HOST_NAME,
            HOST_NAME
    );


    @BeforeEach
    public void setUp() {
        sslContext = sslClientContext(stores,"routing_configurer");
        QpidClientConfig config = new QpidClientConfig(sslContext);
        //TODO messageCollectorUser should not be there...
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate(), "message_collector");
    }

    @Test
    public void messageGoesThroughWithOkTTL() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,sslContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 3000));

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext)) {
            Optional<Message> receive = Optional.ofNullable(sink.createConsumer().receive(1000));
            assertThat(receive).isPresent();
        }

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
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 10000));

        Thread.sleep(6000);

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext)) {
            Optional<Message> receive = Optional.ofNullable(sink.createConsumer().receive(1000));
            assertThat(receive).isNotPresent();
        }

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
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 3000));

        Thread.sleep(4000);

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext)) {
            Optional<Message> receive = Optional.ofNullable(sink.createConsumer().receive(1000));
            assertThat(receive).isNotPresent();
        }

    }

    private JmsMessage createDenmMessage(Source source, byte[] bytemessage, long ttl) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .publicationId("NO-123-pub")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .shardId(1)
                .shardCount(1)
                .causeCode(5)
                .subCauseCode(76)
                .ttl(ttl)
                .build();
    }
}
