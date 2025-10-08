package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.qpid.Binding;
import no.vegvesen.ixn.federation.qpid.Filter;
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
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

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
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("bi-qpid")
            );

    @BeforeEach
    public void setUp() {
        sslContext = sslClientContext(stores,"routing_configurer");
        QpidClientConfig config = new QpidClientConfig(sslContext);
        //TODO messageCollectorUser should not be there...
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate());
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

    @Test
    public void dynamicFilterMatchesOneMessageAndNotAnother() throws Exception{
        String consumeQueue = "bi-queue";
        String deliveryExchange = "del-123456789";
        String capabilityExchange = "cap-123456789";

        String capabilitySelector = "originatingCountry = 'NO'";
        String deliverySelector ="originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12003%' and causeCode = 6";
        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);

        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.createHeadersExchange(capabilityExchange);

        qpidClient.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange, null)); //arguments= new Filter(joinedSelector)?
        qpidClient.addBinding(capabilityExchange, new Binding(capabilityExchange, consumeQueue, null)); //arguments = new Filter(capabilitySelector)?

        AtomicInteger numMessages = new AtomicInteger();

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                consumeQueue,
                sslContext,
                message -> numMessages.incrementAndGet(),
                "originatingCountry = 'NO'"
        )) {
            sink.start();
            try ( Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchange,sslContext)) {
                source.start();
                String messageText = "This is my DENM message :) ";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                source.sendNonPersistentMessage(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("")
                        .publisherId("NO-123")
                        .publicationId("pub-1")
                        .messageType(Constants.DENM)
                        .causeCode(6)
                        .subCauseCode(61)
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12003,")
                        .shardId(1)
                        .shardCount(1)
                        .timestamp(System.currentTimeMillis())
                        .build());
            }
            sink.close();
            sink.start();
            Thread.sleep(200);
        }
        assertThat(numMessages.get()).isEqualTo(2);
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
