package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurerProperties.class})
@ContextConfiguration(initializers = {NewQpidStructureIT.Initializer.class})
@Testcontainers
public class NewQpidStructureIT extends QpidDockerBaseIT {

    private static Logger logger = LoggerFactory.getLogger(NewQpidStructureIT.class);

    private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

    private static KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf");

    @Autowired
    SSLContext sslContext;

    @Autowired
    QpidClient qpidClient;

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);


    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            //need to set the logg follower somewhere, this seems like a "good" place to do it for now
            qpidContainer.followOutput(new Slf4jLogConsumer(logger));
            String httpsUrl = qpidContainer.getHttpsUrl();
            String httpUrl = qpidContainer.getHttpUrl();
            logger.info("server url: " + httpsUrl);
            logger.info("server url: " + httpUrl);
            TestPropertyValues.of(
                    "routing-configurer.baseUrl=" + httpsUrl,
                    "routing-configurer.vhost=localhost",
                    "test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
                    "test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12")
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    public void directExchangeToOutputQueuePOC() throws Exception {
        //1. Create an exchange
        String exchangeName = "inputExchange";
        qpidClient.createDirectExchange(exchangeName);
        String queueName = "outputQueue";
        //2. Create a queue
        qpidClient.createQueue(queueName);
        //2.1 Create a Capability to base a Validating selector on
        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "1.0",
                        new HashSet<>(Arrays.asList("12","13")),
                        new HashSet<>(Arrays.asList(5, 6))
                ),
                new Metadata()
        );
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(capability);
        System.out.println(selector);
        //3. Create a binding on exchange using the validating selector, pointing at queue
        qpidClient.bindDirectExchange(selector, exchangeName, queueName);
        System.out.println(qpidContainer.getHttpUrl());
        //4. Create a Source, sending one good message, and one bad
        AtomicInteger numMessages = new AtomicInteger();
        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                queueName,
                sslContext,
                message -> numMessages.incrementAndGet())) {
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(),exchangeName,sslContext)) {
                //5. Create a Sink, listens to queue. Check that sink get 1 message.
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
                .causeCode("6")
                .subCauseCode("76")
                .build();
    }

    private JmsMessage getJmsMessage(Source source, String originatingCountry, String quadTreeTiles) throws JMSException {
        return source.createMessageBuilder()
                .textMessage("Yo")
                .userId("localhost")
                .messageType(Constants.DATEX_2)
                .publisherId("NO-123")
                .publicationId("pub-1")
                .publicationType("Obstruction")
                .protocolVersion("DATEX2;2.3")
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
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
                SubscriptionStatus.CREATED
        );

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        new HashSet<>(Arrays.asList("12004")),
                        new HashSet<>(Arrays.asList(6))
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
                LocalDeliveryStatus.CREATED
        );

        //1. Creating delivery queue and adding write access.
        qpidClient.createDirectExchange(inQueueName);
        qpidClient.addWriteAccess("king_gustaf", inQueueName);

        //2. Creating read queue and adding read access.
        qpidClient.createQueue(outQueueName);
        qpidClient.addReadAccess("king_gustaf", outQueueName);

        //Create intermediate exchange. No ACL needed for this.
        qpidClient.createTopicExchange(exchangeName);

        //3. Making Capability selector and joining with delivery selector.
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String capabilitySelector = creator.makeSelector(capability);
        System.out.println(capabilitySelector);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        //4. Adding binding on deliveryQueue to out-queue
        qpidClient.bindDirectExchange(joinedSelector,inQueueName,exchangeName);
        qpidClient.bindTopicExchange(subscriptionSelector, exchangeName, outQueueName);

        //5. Adding binding on out queue to outgoingExchange
        //TODO this is not being done. We need a lot of selectors here:
            //1. The capability selector, used for validating incoming messages from SP.
            //2. The delivery selector, further limiting what the SP can produce on the in-queue.
            //3. The subscription selector, event further limiting what ends up on the out-queue for the Subscription.
        //TODO the question becomes: Do we just AND the different selectors together?

        //String subscriptionSelector = subscription.getSelector();
        //System.out.println(subscriptionSelector);

        //qpidClient.addBinding(subscriptionSelector, outQueueName, "" + subscriptionSelector.hashCode(), exchangeName);

        //6. Allowing inQueue To publish on exchange
        //qpidClient.createPublishAccessOnExchangeForQueue("routing_configurer", inQueueName);
        //qpidClient.createConsumeAccessOnQueueForExchange(inQueueName);

        //7. Creating a Source and Sink to send message
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
                        .causeCode("6")
                        .subCauseCode("61")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12004,")
                        .timestamp(System.currentTimeMillis())
                        .build());
                System.out.println();
            }
            System.out.println();
            Thread.sleep(200);
        }
        System.out.println(qpidClient.getQpidAcl().aclAsString());
        assertThat(numMessages.get()).isEqualTo(1);
    }

    @Test
    public void sendMessageFromExchangeToExchange() throws Exception{
        String input = "input_exchange";
        String output = "output_exchange";

        qpidClient.createTopicExchange(input);
        qpidClient.addWriteAccess("king_gustaf", input);

        qpidClient.createTopicExchange(output);

        String selector = "((publisherId = 'NO-123') AND (quadTree like '%,12004%') AND (messageType = 'DENM') AND (causeCode = '6') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6')";

        qpidClient.bindTopicExchange(selector,input,output);

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
                        .causeCode("6")
                        .subCauseCode("61")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12004,")
                        .timestamp(System.currentTimeMillis())
                        .build());
            }
            System.out.println();
            Thread.sleep(200);
        }
        //assertThat(numMessages.get()).isEqualTo(1);
    }

    @Test
    public void testDuplicateMessagesUsingOneDeliveryEndpoint() throws Exception {
        String capabilityExchange1 = "capability-exchange1";
        String capabilityExchange2 = "capability-exchange2";
        String deliveryExchange = "delivery-exchange";
        String subscriptionQueue = "king_gustaf";

        Subscription subscription = new Subscription(
                "originatingCountry = 'NO'",
                SubscriptionStatus.CREATED
        );

        CapabilitySplit capability1 = new CapabilitySplit(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        new HashSet<>(Arrays.asList("12002", "12003")),
                        new HashSet<>(Arrays.asList(6))
                ),
                new Metadata()
        );

        CapabilitySplit capability2 = new CapabilitySplit(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        new HashSet<>(Arrays.asList("12003")),
                        new HashSet<>(Arrays.asList(6))
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

        qpidClient.createTopicExchange(capabilityExchange1);

        qpidClient.createTopicExchange(capabilityExchange2);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String capabilitySelector1 = creator.makeSelector(capability1);
        String capabilitySelector2 = creator.makeSelector(capability2);

        String joinedSelector1 = String.format("(%s) AND (%s)", capabilitySelector1, deliverySelector);
        System.out.println(joinedSelector1);

        String joinedSelector2 = String.format("(%s) AND (%s)", capabilitySelector2, deliverySelector);
        System.out.println(joinedSelector2);

        qpidClient.bindDirectExchange(joinedSelector1,deliveryExchange,capabilityExchange1);
        qpidClient.bindDirectExchange(joinedSelector2,deliveryExchange,capabilityExchange2);
        qpidClient.bindTopicExchange(subscriptionSelector, capabilityExchange1, subscriptionQueue);
        qpidClient.bindTopicExchange(subscriptionSelector, capabilityExchange2, subscriptionQueue);

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
                        .causeCode("6")
                        .subCauseCode("61")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12003,12002,")
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

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        new HashSet<>(Arrays.asList("12003")),
                        new HashSet<>(Arrays.asList(6))
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12003%' and causeCode = '6'",
                LocalDeliveryStatus.CREATED
        );

        //1. Creating delivery queue and adding write access.
        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess("king_gustaf", deliveryExchange);

        //Create intermediate exchange. No ACL needed for this.
        qpidClient.createTopicExchange(capabilityExchange);

        //3. Making Capability selector and joining with delivery selector.
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String capabilitySelector = creator.makeSelector(capability);

        String deliverySelector = delivery.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        //4. Adding binding on deliveryQueue to out-queue
        qpidClient.bindDirectExchange(joinedSelector,deliveryExchange,capabilityExchange);
        qpidClient.bindTopicExchange(capabilitySelector, capabilityExchange, consumeQueue);

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
                        .causeCode("6")
                        .subCauseCode("61")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12003,")
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
