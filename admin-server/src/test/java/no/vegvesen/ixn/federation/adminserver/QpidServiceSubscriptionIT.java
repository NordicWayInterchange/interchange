package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedSubscriptionApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilityMatchApi;
import no.vegvesen.ixn.federation.adminserver.qpid.AdminQpidClient;
import no.vegvesen.ixn.federation.adminserver.qpid.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.docker.DockerBaseIT.getDockerHost;
import static no.vegvesen.ixn.docker.DockerBaseIT.getTargetFolderPathForTestClass;
import static no.vegvesen.ixn.docker.QpidDockerBaseIT.*;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("This test must be completed after rewriting the capability application stuff")
@Testcontainers
public class QpidServiceSubscriptionIT {
    public static final String HOST_NAME = getDockerHost();

    private static final String CLIENT_USER = "admin_server";
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(QpidServiceIT.class), "my_ca", HOST_NAME, CLIENT_USER);

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    );

    private AdminQpidClient client;

    private QpidService service;

    @BeforeEach
    public void setupClient() {
        SSLContext sslContext = sslClientContext(stores, CLIENT_USER);
        //client = new AdminQpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),createRestTemplate(sslContext));
        service = new QpidService(client);
    }

    @Test
    public void testGetCapabilitiesLinkedSubscription() {
        String spName = "sp-1";
        DenmApplication application = new DenmApplication(
                1,
                "NO0000",
                "NO0000:001",
                "NO",
                "1.0",
                List.of("123"),
                List.of(6)
        );
        Capability capability = new Capability(
                "123",
                application,
                //new Metadata(1),
                new Metadata(),
                List.of(new CapabilityShard(
                        1,
                        "shard-exchange-1",
                        //MessageValidatingSelectorCreator.makeSelector(application,null)
                        "a = b" //TODO use the above line after rewriting
                ))
        );
        LocalSubscription subscription = new LocalSubscription(
                "1234",
                LocalSubscriptionStatus.CREATED,
                "publicationId = 'NO0000:001'",
                spName,
                Set.of(),
                Set.of()
        );
        CapabilitiesLinkedSubscriptionApi capabilitiesLinkedSubscription = service.getCapabilitiesLinkedSubscription(subscription, Set.of(capability));
        assertThat(capabilitiesLinkedSubscription.capabilityMatchApi().size()).isEqualTo(1);
        CapabilityMatchApi match = capabilitiesLinkedSubscription.capabilityMatchApi().getFirst();
    }


}
