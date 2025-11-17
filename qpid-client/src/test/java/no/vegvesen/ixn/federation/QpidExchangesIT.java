package no.vegvesen.ixn.federation;

import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.shared.Constants;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class QpidExchangesIT extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(QpidExchangesIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");


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
        sslContext = sslClientContext(stores,"routing_configurer");
        QpidClientConfig config = new QpidClientConfig(sslContext);
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate());
    }
    @Test
    public void testHeadersExchangeWithRandomBinding() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        Exchange inExchange = qpidClient.createHeadersExchange("delex");
        Exchange capExhange = qpidClient.createHeadersExchange("capex");
        Queue subscriptionQueue = qpidClient.createQueue("loc");

        qpidClient.addBinding(inExchange.getName(),new Binding("yoyoyo", capExhange.getName(), null));
        qpidClient.addBinding(capExhange.getName(),new Binding(subscriptionQueue.getName(), subscriptionQueue.getName(), null));

        assertThat(sendAndReceive(inExchange.getName(), subscriptionQueue.getName())).isTrue();
    }

    @Test
    public void testDirectExchangeWithRandomBinding() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        Exchange inExchange = qpidClient.createDirectExchange("delex");
        Exchange capExhange = qpidClient.createHeadersExchange("capex");
        Queue subscriptionQueue = qpidClient.createQueue("loc");

        qpidClient.addBinding(inExchange.getName(),new Binding("yoyoyo", capExhange.getName(), null));
        qpidClient.addBinding(capExhange.getName(),new Binding(subscriptionQueue.getName(), subscriptionQueue.getName(), null));

        assertThat(sendAndReceive(inExchange.getName(), subscriptionQueue.getName())).isFalse();
    }

    @Test
    public void testDirectExchangeWithBindingLikeSourceName() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        Exchange inExchange = qpidClient.createDirectExchange("delex");
        Exchange capExhange = qpidClient.createHeadersExchange("capex");
        Queue subscriptionQueue = qpidClient.createQueue("loc");

        qpidClient.addBinding(inExchange.getName(),new Binding(inExchange.getName(), capExhange.getName(), null));
        qpidClient.addBinding(capExhange.getName(),new Binding(subscriptionQueue.getName(), subscriptionQueue.getName(), null));

        assertThat(sendAndReceive(inExchange.getName(), subscriptionQueue.getName())).isTrue();
    }

    @Test
    public void testDirectExchangeWithAlternateBindingAndBindLikeSourceName() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        //dlqueue er allerede laget, ligger i config.json
        Exchange inExchange = qpidClient.createDirectExchangeWithDlq("delex", "dlqueue");
        Exchange capExhange = qpidClient.createHeadersExchange("capex");
        Queue subscriptionQueue = qpidClient.createQueue("loc");

        qpidClient.addBinding(inExchange.getName(),new Binding(inExchange.getName(), capExhange.getName(), null));
        qpidClient.addBinding(capExhange.getName(),new Binding(subscriptionQueue.getName(), subscriptionQueue.getName(), null));

        assertThat(sendAndReceive(inExchange.getName(), subscriptionQueue.getName())).isTrue();
    }

    @Test
    public void testDirectExchangeWithAlternateBindingAndBindNotLikeSourceName() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        //dlqueue er allerede laget, ligger i config.json
        Exchange inExchange = qpidClient.createDirectExchangeWithDlq("delex", "dlqueue");
        Exchange capExhange = qpidClient.createHeadersExchange("capex");
        Queue subscriptionQueue = qpidClient.createQueue("loc");

        qpidClient.addBinding(inExchange.getName(),new Binding("notedlex", capExhange.getName(), null));
        qpidClient.addBinding(capExhange.getName(),new Binding(subscriptionQueue.getName(), subscriptionQueue.getName(), null));

        assertThat(sendAndReceive(inExchange.getName(), subscriptionQueue.getName())).isFalse();
    }

    private boolean sendAndReceive(String inExchangeName, String outQueue) throws Exception {
        WriteToScreenMessageListener writeToScreenMessageListener = new WriteToScreenMessageListener();
        CountDownLatch latch = new CountDownLatch(1);
        boolean success;
        MessageListener messageListener = message -> {
            writeToScreenMessageListener.onMessage(message);
            latch.countDown();
        };
        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), outQueue,sslContext, messageListener)){
            sink.start();
            try (Source source = new Source(qpidContainer.getAmqpsUrl(), inExchangeName,sslContext)){
                source.start();
                JmsMessage denmMessage = createDenmMessage(source, "This is a test".getBytes(StandardCharsets.UTF_8), 3000);
                source.sendNonPersistentMessage(denmMessage);
            }
            success = latch.await(3, TimeUnit.SECONDS);
        }
        return success;
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
