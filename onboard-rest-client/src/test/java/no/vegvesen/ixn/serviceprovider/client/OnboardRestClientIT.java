package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.net.ssl.SSLContext;
import java.util.Collections;

public class OnboardRestClientIT extends DockerBaseIT {

    private static Logger log = LoggerFactory.getLogger(OnboardRestClientIT.class);

    public static Network network;

    public static PostgreSQLContainer dbContainer =
            new PostgreSQLContainer<>("postgres:9.6")
                    .withDatabaseName("federation")
                    .withUsername("federation")
                    .withPassword("federation");

    public static GenericContainer onboardServer;

    @BeforeClass
    public static void startUp() {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
        network = Network.newNetwork();
        dbContainer.withNetwork(network)
                .withNetworkAliases("database")
                .start();
        onboardServer = new GenericContainer(
                    new ImageFromDockerfile()
                            .withFileFromPath(".",
                                    getFolderPath("onboard-server")
                            )
                    )
                    .withNetwork(network)
                    .withClasspathResourceMapping("jks/server","/jks", BindMode.READ_ONLY)
                    .withEnv("KEY_STORE","/jks/localhost.p12")
                    .withEnv("KEY_STORE_PASSWORD","password")
                    .withEnv("TRUST_STORE_PASSWORD","password")
                    .withEnv("TRUST_STORE","/jks/truststore.jks")
                    .withEnv("SERVER_NAME","localhost")
                    .withEnv("SP_CHNL_PORT","8899")
                    .withEnv("POSTGRES_URI","jdbc:postgresql://database/federation")
                    .withExposedPorts(8899);
        onboardServer
                .start();
        onboardServer.followOutput(logConsumer);
    }

    @AfterClass
    public static void shutdown() {
        onboardServer.stop();
        dbContainer.stop();
    }

    private OnboardRESTClient client;
    private final SSLContext sslContext = TestKeystoreHelper.sslContext("jks/client/onboard.p12", "jks/client/truststore.jks");


    @Before
    public void setUp() {
        client = new OnboardRESTClient(sslContext,"https://localhost:" + onboardServer.getMappedPort(8899),"onboard");
    }


    private String getServerUri() {
        return "https://localhost:" + onboardServer.getMappedPort(8899);
    }

    @Test
    public void addCapabilityCheckAndDelete() throws JsonProcessingException {
        CapabilityApi capabilities = new CapabilityApi();
        capabilities.setName("onboard");

        Datex2DataTypeApi datexNO = new Datex2DataTypeApi();
        datexNO.setOriginatingCountry("NO");
        capabilities.setCapabilities(Collections.singleton(datexNO));
        client.addCapabilities(capabilities);

        ObjectMapper objectMapper = new ObjectMapper();
        CapabilityApi newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));

        client.deleteCapability(newCapabilities);
        newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));

    }

    @Test
    public void addSubscriptionCheckAndDelete() throws JsonProcessingException {
        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
        subscriptionRequestApi.setName("onboard");
        SubscriptionApi subscription = new SubscriptionApi();
        subscription.setSelector("originatingCountry = 'NO'");
        subscriptionRequestApi.setSubscriptions(Collections.singleton(subscription));
        client.addSubscriptions(subscriptionRequestApi);

        SubscriptionRequestApi newSubscriptionRequest = client.getServiceProviderSubscriptionRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(newSubscriptionRequest));

        newSubscriptionRequest = client.deleteSubscriptions(newSubscriptionRequest);
        System.out.println(objectMapper.writeValueAsString(newSubscriptionRequest));

    }

    @Test
    @Ignore
    public void addSubscriptionAskForCapabilities() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
        subscriptionRequestApi.setName("onboard");
        SubscriptionApi subscription = new SubscriptionApi();
        subscription.setSelector("originatingCountry = 'NO'");
        subscriptionRequestApi.setSubscriptions(Collections.singleton(subscription));
        SubscriptionRequestApi subscriptions = client.addSubscriptions(subscriptionRequestApi);
        System.out.println(objectMapper.writeValueAsString(subscriptions));

        CapabilityApi capabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(capabilities));

        client.deleteSubscriptions(subscriptions);
    }

    @Test
    public void addSubscriptionAskForCapabilities2() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
        subscriptionRequestApi.setName("onboard");
        SubscriptionApi subscription = new SubscriptionApi();
        subscription.setSelector("originatingCountry = 'NO'");
        subscriptionRequestApi.setSubscriptions(Collections.singleton(subscription));
        SubscriptionRequestApi subscriptions = client.addSubscriptions(subscriptionRequestApi);
        System.out.println(objectMapper.writeValueAsString(subscriptions));

        CapabilityApi capabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(capabilities));

        subscriptions = client.getServiceProviderSubscriptionRequest();
        client.deleteSubscriptions(subscriptions);
    }

}
