package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class OnboardRestClientIT extends DockerBaseIT {

    private static Logger log = LoggerFactory.getLogger(OnboardRestClientIT.class);
    private static Path testKeysPath = generateKeys(OnboardRestClientIT.class, "my_ca", "localhost", "onboard");

    public static Network network;

    public static PostgreSQLContainer dbContainer =
            new PostgreSQLContainer<>("postgres:9.6")
                    .withDatabaseName("federation")
                    .withUsername("federation")
                    .withPassword("federation");

    public static GenericContainer onboardServer;
    public static final String USER = "onboard";

    @BeforeAll
    public static void startUp() {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
        network = Network.newNetwork();
        dbContainer.withNetwork(network)
                .withNetworkAliases("database")
                .start();
        onboardServer = new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromPath(".",
                                getFolderPath("onboard-server-app")
                        )
        )
                .withNetwork(network)
                .withFileSystemBind(testKeysPath.toString(), "/jks", BindMode.READ_ONLY)
                .withEnv("KEY_STORE", "/jks/localhost.p12")
                .withEnv("KEY_STORE_PASSWORD", "password")
                .withEnv("TRUST_STORE_PASSWORD", "password")
                .withEnv("TRUST_STORE", "/jks/truststore.jks")
                .withEnv("SERVER_NAME", "localhost")
                .withEnv("SP_CHNL_PORT", "8899")
                .withEnv("POSTGRES_URI", "jdbc:postgresql://database/federation")
                .withExposedPorts(8899);
        onboardServer
                .start();
        onboardServer.followOutput(logConsumer);
    }

    @AfterAll
    public static void shutdown() {
        onboardServer.stop();
        dbContainer.stop();
    }

    private OnboardRESTClient client;
    private final SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "onboard.p12", "truststore.jks");


    @BeforeEach
    public void setUp() {
        client = new OnboardRESTClient(sslContext, "https://localhost:" + onboardServer.getMappedPort(8899), USER);
    }


    @Test
    public void addCapabilityCheckAndDelete() throws JsonProcessingException {

        DatexCapabilityApi datexNO = new DatexCapabilityApi("NO");
        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                USER,
                Collections.singleton(datexNO)
        );
        client.addCapability(request);

        ObjectMapper objectMapper = new ObjectMapper();
        ListCapabilitiesResponse newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));
        assertThat(newCapabilities.getCapabilities()).hasSize(1);

        LocalActorCapability localActorCapability = newCapabilities.getCapabilities().iterator().next();
        String id = localActorCapability.getId();
        client.deleteCapability(id);

        newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));
    }

    @Test
    public void addSubscriptionCheckAndDelete() throws JsonProcessingException {

        SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'");
        client.addSubscription(new AddSubscriptionsRequest(USER, Collections.singleton(selectorApi)));

        ListSubscriptionsResponse localSubscriptions = client.getServiceProviderSubscriptions();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(localSubscriptions));

        assertThat(localSubscriptions).isNotNull();
        List<LocalActorSubscription> filtered = filterOutTearDownSubscriptions(localSubscriptions.getSubscriptions());
        assertThat(filtered).isNotNull().hasSize(1);
        LocalActorSubscription idSubToDelete = filtered.get(0);

        client.deleteSubscriptions(idSubToDelete.getId());
        ListSubscriptionsResponse afterDelete = client.getServiceProviderSubscriptions();
        List<LocalActorSubscription> filteredAfterDelete = filterOutTearDownSubscriptions(afterDelete.getSubscriptions());
        assertThat(filteredAfterDelete).hasSize(0);
    }

    @Test
    public void addSubscriptionAskForCapabilities() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'");
        AddSubscriptionsResponse addedSubscription = client.addSubscription(
                new AddSubscriptionsRequest(USER,Collections.singleton(selectorApi))
        );
        System.out.println(objectMapper.writeValueAsString(addedSubscription));

        ListCapabilitiesResponse capabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(capabilities));

        ListSubscriptionsResponse serviceProviderSubscriptionRequest = client.getServiceProviderSubscriptions();
        for (LocalActorSubscription subscription : serviceProviderSubscriptionRequest.getSubscriptions()) {
            System.out.println("deleting subscription " + subscription.getId());
            client.deleteSubscriptions(subscription.getId());
        }
        ListSubscriptionsResponse afterDelete = client.getServiceProviderSubscriptions();
        assertThat(filterOutTearDownSubscriptions(afterDelete.getSubscriptions())).hasSize(0);
    }


    private List<LocalActorSubscription> filterOutTearDownSubscriptions(Set<LocalActorSubscription> subscriptions) {
        return subscriptions.stream().filter(sub -> !sub.getStatus().equals(LocalActorSubscriptionStatusApi.NOT_VALID)).collect(Collectors.toList());
    }

}
