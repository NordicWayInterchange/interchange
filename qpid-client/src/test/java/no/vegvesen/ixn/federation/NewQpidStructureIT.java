package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;

import jakarta.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


//TODO this class does things differently from all the other Qpid test classes. Should we change this to be like the rest?
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurerProperties.class})
public class NewQpidStructureIT extends QpidDockerBaseIT {

    private static final Logger logger = LoggerFactory.getLogger(NewQpidStructureIT.class);

    public static final String HOSTNAME = "localhost";

    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(NewQpidStructureIT.class),"my_ca", HOSTNAME, "routing_configurer", "king_gustaf");

    @Qualifier("getTestSslContext")
    @Autowired
    SSLContext sslContext;

    @Autowired
    QpidClient qpidClient;

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOSTNAME,
            HOSTNAME,
            Path.of("qpid")
            );

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        qpidContainer.followOutput(new Slf4jLogConsumer(logger));
        registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
        registry.add("routing-configurer.vhost", () -> "localhost");
        registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
        registry.add("test.ssl.key-store", () -> getClientStorePath("routing_configurer", stores.clientStores()));
    }

    @BeforeAll
    static void setup(){
        qpidContainer.start();
    }


    @Test
    public void directExchangeToOutputQueuePOC() throws Exception {
        String exchangeName = "inputExchange";
        qpidClient.createDirectExchange(exchangeName);
        String queueName = "outputQueue";
        qpidClient.createQueue(queueName);

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("12","13"),
                        List.of(5, 6)
                ),
                new Metadata()
        );
        String selector = MessageValidatingSelectorCreator.makeSelector(capability);

        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));
        System.out.println(qpidContainer.getHttpUrl());

        AtomicInteger numMessages = new AtomicInteger();
        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                queueName,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),exchangeName,sslContext)) {
                source.start();
                source.sendNonPersistentMessage(getJmsMessage(source, "NO", ",1234,"));
                String messageText = "{}";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                source.sendNonPersistentMessage(createMonotchMessage(source, bytemessage));
                source.sendNonPersistentMessage(getJmsMessage(source, "SE", ",1134,"));
            }
            Thread.sleep(200);
        }
        assertThat(numMessages.get()).isEqualTo(1);
    }

    private JmsMessage createMonotchMessage(Source source, byte[] bytemessage) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .publicationId("pub-1")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .shardId(1)
                .shardCount(1)
                .causeCode(6)
                .subCauseCode(76)
                .build();
    }

    private JmsMessage getJmsMessage(Source source, String originatingCountry, String quadTreeTiles) throws JMSException {
        return source.createMessageBuilder()
                .textMessage("Yo")
                .userId(HOSTNAME)
                .messageType(Constants.DATEX_2)
                .publisherId("NO-123")
                .publicationId("pub-1")
                .publicationType("Obstruction")
                .publisherName("publishername")
                .protocolVersion("DATEX2;2.3")
                .shardId(1)
                .shardCount(1)
                .latitude(60.352374)
                .longitude(13.334253)
                .originatingCountry(originatingCountry)
                .quadTreeTiles(quadTreeTiles)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Test
    public void testUseOfDeliveryQueueForSendingToOutgoingExchange() throws Exception {
        String exchangeName = "intermediate-exchange";
        String inQueueName = "delivery-exchange";
        String outQueueName = "king_gustaf";

        Subscription subscription = new Subscription(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
                SubscriptionStatus.CREATED
        );

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12004"),
                        List.of(6)
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
                LocalDeliveryStatus.CREATED
        );

        qpidClient.createDirectExchange(inQueueName);
        qpidClient.addWriteAccess("king_gustaf", inQueueName);

        qpidClient.createQueue(outQueueName);
        qpidClient.addReadAccess("king_gustaf", outQueueName);

        qpidClient.createHeadersExchange(exchangeName);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability);
        System.out.println(capabilitySelector);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        qpidClient.addBinding(inQueueName, new Binding(inQueueName, exchangeName, new Filter(joinedSelector)));
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, outQueueName, new Filter(subscriptionSelector)));

        AtomicInteger numMessages = new AtomicInteger();

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                outQueueName,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),inQueueName,sslContext)) {
                source.start();
                String messageText = "This is my DENM message :) ";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                source.sendNonPersistentMessage(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("kong_olav")
                        .publisherId("NO-123")
                        .publicationId("pub-1")
                        .messageType(Constants.DENM)
                        .causeCode(6)
                        .subCauseCode(61)
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12004,")
                        .shardId(1)
                        .shardCount(1)
                        .timestamp(System.currentTimeMillis())
                        .build());
                System.out.println();
            }
            System.out.println();
            Thread.sleep(200);
        }
        System.out.println(qpidClient.getQpidAcl());
        assertThat(numMessages.get()).isEqualTo(1);
    }

    @Test
    public void sendMessageFromExchangeToExchange() throws Exception{
        String input = "input_exchange";
        String output = "output_exchange";

        qpidClient.createHeadersExchange(input);
        qpidClient.addWriteAccess("king_gustaf", input);

        qpidClient.createHeadersExchange(output);

        String selector = "((publisherId = 'NO-123') AND (quadTree like '%,12004%') AND (messageType = 'DENM') AND (causeCode = 6) AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6)";

        qpidClient.addBinding(input, new Binding(input, output, new Filter(selector)));

        AtomicInteger numMessages = new AtomicInteger();
        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                output,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),input,sslContext)) {
                source.start();
                String messageText = "This is my DENM message :) ";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                source.sendNonPersistentMessage(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("king_gustaf")
                        .publisherId("NO-123")
                        .publicationId("NO-123-pub")
                        .messageType(Constants.DENM)
                        .causeCode(6)
                        .subCauseCode(61)
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12004,")
                        .shardId(1)
                        .shardCount(1)
                        .timestamp(System.currentTimeMillis())
                        .build());
            }
            System.out.println();
            Thread.sleep(200);
        }
    }

    @Test
    public void testDuplicateMessagesUsingOneDeliveryEndpoint() throws Exception {
        String capabilityExchange1 = "capability-exchange1";
        String capabilityExchange2 = "capability-exchange2";
        String deliveryExchange = "delivery-exchange-test-duplicate";
        String subscriptionQueue = "king_gustaf_output_queue";

        Subscription subscription = new Subscription(
                "originatingCountry = 'NO'",
                SubscriptionStatus.CREATED
        );

        Capability capability1 = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12002", "12003"),
                        List.of(6)
                ),
                new Metadata()
        );

        Capability capability2 = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12003"),
                        List.of(6)
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED
        );

        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess("king_gustaf", deliveryExchange);

        qpidClient.createQueue(subscriptionQueue);
        qpidClient.addReadAccess("king_gustaf", subscriptionQueue);

        qpidClient.createHeadersExchange(capabilityExchange1);

        qpidClient.createHeadersExchange(capabilityExchange2);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String capabilitySelector1 = MessageValidatingSelectorCreator.makeSelector(capability1);
        String capabilitySelector2 = MessageValidatingSelectorCreator.makeSelector(capability2);

        String joinedSelector1 = String.format("(%s) AND (%s)", capabilitySelector1, deliverySelector);
        System.out.println(joinedSelector1);

        String joinedSelector2 = String.format("(%s) AND (%s)", capabilitySelector2, deliverySelector);
        System.out.println(joinedSelector2);

        qpidClient.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange1, new Filter(joinedSelector1)));
        qpidClient.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange2, new Filter(joinedSelector2)));
        qpidClient.addBinding(capabilityExchange1, new Binding(capabilityExchange1, subscriptionQueue, new Filter(subscriptionSelector)));
        qpidClient.addBinding(capabilityExchange2, new Binding(capabilityExchange2, subscriptionQueue, new Filter(subscriptionSelector)));

        AtomicInteger numMessages = new AtomicInteger();

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                subscriptionQueue,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchange,sslContext)) {
                source.start();
                String messageText = "This is my DENM message :) ";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                source.sendNonPersistentMessage(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("kong_olav")
                        .publisherId("NO-123")
                        .publicationId("pub-1")
                        .messageType(Constants.DENM)
                        .causeCode(6)
                        .subCauseCode(61)
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12003,12002,")
                        .shardId(1)
                        .shardCount(1)
                        .timestamp(System.currentTimeMillis())
                        .build());
                System.out.println();
            }
            System.out.println();
            Thread.sleep(200);
        }
        System.out.println(numMessages.get());
        assertThat(numMessages.get()).isEqualTo(1);
    }

    @Test
    public void consumeFromQueueWithNonDestructiveConsumers() throws Exception{
        String consumeQueue = "bi-queue";
        String deliveryExchange = "del-123456789";
        String capabilityExchange = "cap-123456789";

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12003"),
                        List.of(6)
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12003%' and causeCode = 6",
                LocalDeliveryStatus.CREATED
        );

        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess("king_gustaf", deliveryExchange);

        qpidClient.createHeadersExchange(capabilityExchange);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability);

        String deliverySelector = delivery.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        qpidClient.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange, new Filter(joinedSelector)));
        qpidClient.addBinding(capabilityExchange, new Binding(capabilityExchange, consumeQueue, new Filter(capabilitySelector)));

        AtomicInteger numMessages = new AtomicInteger();

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                consumeQueue,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchange,sslContext)) {
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
                System.out.println();
            }
            System.out.println();
            sink.close();
            sink.start();
            Thread.sleep(200);
        }
        assertThat(numMessages.get()).isEqualTo(2);
    }
}
