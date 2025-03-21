package no.vegvesen.ixn.federation;

import jakarta.jms.*;
import jakarta.jms.Connection;
import no.vegvesen.ixn.*;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.model.MessageValidator;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.message.AMQMessageHeader;
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

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


//TODO this class does things differently from all the other Qpid test classes. Should we change this to be like the rest?
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurerProperties.class})
public class NewQpidStructureIT extends QpidDockerBaseIT {

    private static final Logger logger = LoggerFactory.getLogger(NewQpidStructureIT.class);

    public static final String HOSTNAME = "localhost";

    private static final String SP_NAME = "king_gustaf";
    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(NewQpidStructureIT.class),"my_ca", HOSTNAME, "routing_configurer", SP_NAME);

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
            ).withLogConsumer(new Slf4jLogConsumer(logger));

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
        String selector = MessageValidatingSelectorCreator.makeSelector(capability, null);
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));
        System.out.println(qpidContainer.getHttpUrl());

        CountingMessageListener listener = new CountingMessageListener();
        SimpleConnectionCreator connectionCreator = new SimpleConnectionCreator(sslContext);

        try (Connection connection = connectionCreator.createConnection(qpidContainer.getAmqpsUrl())) {
            connection.start();
            try (Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = session.createQueue(queueName);
                try (MessageConsumer consumer = session.createConsumer(destination)) {
                    consumer.setMessageListener(listener);
                    try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
                        source.start();
                        source.sendNonPersistentMessage(getJmsMessage(source, "NO", ",1234,"));
                        String messageText = "{}";
                        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                        source.sendNonPersistentMessage(createMonotchMessage(source, bytemessage));
                        source.sendNonPersistentMessage(getJmsMessage(source, "SE", ",1134,"));
                    }
                    listener.releaseLockAfter(200,TimeUnit.MILLISECONDS);
                }
            }
        }
        assertThat(listener.getCount()).isEqualTo(1);
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
        String outQueueName = SP_NAME;

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
                LocalDeliveryStatus.CREATED,
                "DENM delivery"
        );

        qpidClient.createDirectExchange(inQueueName);
        qpidClient.addWriteAccess(SP_NAME, inQueueName);

        qpidClient.createQueue(outQueueName);
        qpidClient.addReadAccess(SP_NAME, outQueueName);

        qpidClient.createHeadersExchange(exchangeName);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, null);
        System.out.println(capabilitySelector);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        qpidClient.addBinding(inQueueName, new Binding(inQueueName, exchangeName, new Filter(joinedSelector)));
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, outQueueName, new Filter(subscriptionSelector)));


        CountDownMessageListener listener = new CountDownMessageListener(1);
        ConnectionCreator connectionCreator = new SimpleConnectionCreator(sslContext);
        try (Connection connection = connectionCreator.createConnection(qpidContainer.getAmqpsUrl())) {
            connection.start();
            try (Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = session.createQueue(outQueueName);
                try (MessageConsumer consumer = session.createConsumer(destination)) {
                    consumer.setMessageListener(listener);
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
                    }
                }
            }

            assertThat(listener.waitFor(200, TimeUnit.MILLISECONDS)).isTrue();
        }
        System.out.println(qpidClient.getQpidAcl());
    }

    @Test
    public void sendMessageFromExchangeToExchange() throws Exception{
        String input = "input_exchange";
        String output = "output_exchange";

        qpidClient.createHeadersExchange(input);
        qpidClient.addWriteAccess(SP_NAME, input);

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
                        .userId(SP_NAME)
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
                LocalDeliveryStatus.CREATED,
                "NO Delivery"
        );

        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess(SP_NAME, deliveryExchange);

        qpidClient.createQueue(subscriptionQueue);
        qpidClient.addReadAccess(SP_NAME, subscriptionQueue);

        qpidClient.createHeadersExchange(capabilityExchange1);

        qpidClient.createHeadersExchange(capabilityExchange2);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String capabilitySelector1 = MessageValidatingSelectorCreator.makeSelector(capability1, null);
        String capabilitySelector2 = MessageValidatingSelectorCreator.makeSelector(capability2, null);

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
                LocalDeliveryStatus.CREATED,
                "DENM delivery"
        );

        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess(SP_NAME, deliveryExchange);

        qpidClient.createHeadersExchange(capabilityExchange);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, null);

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

    @Test
    public void testExchangeToTwoQueues() throws JMSException, NamingException, InterruptedException, ParseException {
        System.out.println(qpidContainer.getHttpUrl());
        String subscriptionSelector1 = "originatingCountry = 'NO'";

        String subscriptionSelector2 = "originatingCountry = 'NO'";
        Capability capability1 = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12002", "12003"),
                        List.of(6)
                ),
                new Metadata(
                        null,
                        1,
                        RedirectStatus.OPTIONAL,
                        null,
                        null,
                        null
                )
        );

        String deliverySelector = "originatingCountry = 'NO'";

        String deliveryExchange = UUID.randomUUID().toString();
        qpidClient.createDirectExchange(deliveryExchange);
        qpidClient.addWriteAccess(SP_NAME,deliveryExchange);

        String capabilityExchange = UUID.randomUUID().toString();
        qpidClient.createHeadersExchange(capabilityExchange);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability1,null);
        String joinedSelector = String.format("(( %s ) AND ( %s ))", capabilitySelector,deliverySelector);
        System.out.println(joinedSelector);
       qpidClient.addBinding(deliveryExchange,new Binding(deliveryExchange,capabilityExchange, new Filter(joinedSelector)));

        String subscriptionQuque1 = UUID.randomUUID().toString();
        String subscriptionQueue2 = UUID.randomUUID().toString();
        qpidClient.createQueue(subscriptionQuque1);
        qpidClient.addReadAccess(SP_NAME, subscriptionQuque1);
        qpidClient.addBinding(capabilityExchange,new Binding(capabilityExchange,subscriptionQuque1,new Filter(subscriptionSelector1)));

        qpidClient.createQueue(subscriptionQueue2);
        qpidClient.addReadAccess(SP_NAME, subscriptionQueue2);
        qpidClient.addBinding(capabilityExchange,new Binding(capabilityExchange,subscriptionQueue2,new Filter(subscriptionSelector2)));

        try (Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchange,sslContext)) {
            source.start();
            String messageText = "This is my DENM message :) ";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            JmsMessage message = source.createMessageBuilder()
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
                    .build();
            boolean valid = new MessageValidator().isValid(message);
            assertThat(valid).isTrue();
            Set<Capability> capabilities = CapabilityMatcher.matchCapabilitiesToSelector(Set.of(capability1), capabilitySelector);
            assertThat(capabilities).hasSize(1);

            FilterWrapper wrappedMessage = new FilterWrapper(message);
            assertThat(new JMSSelectorFilter(deliverySelector).matches(wrappedMessage)).isTrue();
            assertThat(new JMSSelectorFilter(joinedSelector).matches(wrappedMessage)).isTrue();
            assertThat(new JMSSelectorFilter(subscriptionSelector1).matches(wrappedMessage)).isTrue();
            assertThat(new JMSSelectorFilter(subscriptionSelector2).matches(wrappedMessage)).isTrue();

            source.sendNonPersistentMessage(message);


            ConnectionCreator connectionCreator = new SimpleConnectionCreator(sslContext);

            CountingMessageListener listener1 = new CountingMessageListener();
            CountingMessageListener listener2 = new CountingMessageListener();
            try (Connection connection = connectionCreator.createConnection(qpidContainer.getAmqpsUrl())) {
                //Need two runnables, one for each session/consumer

                Session session1 = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                Destination destination1 = session1.createQueue(subscriptionQuque1);
                MessageConsumer consumer1 = session1.createConsumer(destination1);
                consumer1.setMessageListener(listener1);

                Session session2 = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                Destination destination2 = session2.createQueue(subscriptionQueue2);
                MessageConsumer consumer2 = session2.createConsumer(destination2);
                consumer2.setMessageListener(listener2);

                TimeUnit.SECONDS.sleep(1);
                listener1.releaseLock();
                listener2.releaseLock();
            }
            assertThat(listener1.getCount()).isEqualTo(1);
            assertThat(listener2.getCount()).isEqualTo(1);



        }

    }


    private static class FilterWrapper implements Filterable {

        private final JmsMessage message;

        public FilterWrapper(JmsMessage message) {
            this.message = message;
        }

        @Override
        public AMQMessageHeader getMessageHeader() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public boolean isPersistent() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public boolean isRedelivered() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public Object getHeader(String name) {
            try {
                return message.getFacade().getProperty(name);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getReplyTo() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public String getType() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public byte getPriority() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public String getMessageId() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public long getTimestamp() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public String getCorrelationId() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public long getExpiration() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public Object getConnectionReference() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public long getMessageNumber() {
            throw new IllegalArgumentException("Not implemented");
        }

        @Override
        public long getArrivalTime() {
            throw new IllegalArgumentException("Not implemented");
        }
    }

}
