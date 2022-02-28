package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Testcontainers
@Disabled
public class SendMultipleMessagesIT {


    @Container
    KeysContainer keysContainer = DockerBaseIT.getKeyContainer(SendMultipleMessagesIT.class,"my-domain","localhost","sender","receiver");

    @Container
    QpidContainer qpidContainer = QpidDockerBaseIT.getQpidTestContainer(
            "scheduler-qpid",
            keysContainer.getKeyFolderOnHost(),
            "localhost.p12",
            "password",
            "truststore.jks",
            "password",
            "localhost"
    ).dependsOn(keysContainer);

    @Test
    public void testSceduledMesssageSender() throws NamingException, JMSException {
        SSLContext context = TestKeystoreHelper.sslContext(
                keysContainer.getKeyFolderOnHost(),
                "sender.p12",
                "truststore.jks"
        );
        String amqpsUrl = qpidContainer.getAmqpsUrl();
        try (Source source = new Source(amqpsUrl,"test-queue",context)) {
            source.start();
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            CountDownLatch countDownLatch = new CountDownLatch(10);
            service.scheduleAtFixedRate(() -> {
                        try {
                            System.out.println("Sending!");
                            source.send(source.createMessageBuilder()
                                    .textMessage("Work, goddamit!")
                                    .userId("sender")
                                    .messageType(Constants.DATEX_2)
                                    .publisherId("NO-123")
                                    .originatingCountry("NO")
                                    .protocolVersion("0.1")
                                    .build()
                            );
                            countDownLatch.countDown();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    , 1, 1, TimeUnit.SECONDS);
            countDownLatch.await();
            service.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
