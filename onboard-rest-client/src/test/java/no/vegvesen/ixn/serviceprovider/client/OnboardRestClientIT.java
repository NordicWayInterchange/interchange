package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class OnboardRestClientIT extends DockerBaseIT {

	private static Logger log = LoggerFactory.getLogger(OnboardRestClientIT.class);

    public static Network network;

    public static PostgreSQLContainer dbContainer =
            new PostgreSQLContainer<>("postgres:9.6")
                    .withDatabaseName("federation")
                    .withUsername("federation")
                    .withPassword("federation");

    public static GenericContainer onboardServer;

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

    @AfterAll
    public static void shutdown() {
        onboardServer.stop();
        dbContainer.stop();
    }

    private OnboardRESTClient client;
    private final SSLContext sslContext = TestKeystoreHelper.sslContext("jks/client/onboard.p12", "jks/client/truststore.jks");


    @BeforeEach
    public void setUp() {
        client = new OnboardRESTClient(sslContext,"https://localhost:" + onboardServer.getMappedPort(8899),"onboard");
    }


    @Test
    public void addCapabilityCheckAndDelete() throws JsonProcessingException {

        Datex2DataTypeApi datexNO = new Datex2DataTypeApi("NO");
        client.addCapability(datexNO);

        ObjectMapper objectMapper = new ObjectMapper();
        LocalDataTypeList newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));

		LocalDataType localDataType = newCapabilities.getDataTypes().iterator().next();
		client.deleteCapability(localDataType.getId());

        newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));
    }

    @Test
    public void addSubscriptionCheckAndDelete() throws JsonProcessingException {
		client.addSubscription(new Datex2DataTypeApi("NO"));

        LocalSubscriptionListApi localSubscriptions = client.getServiceProviderSubscription();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(localSubscriptions));

        assertThat(localSubscriptions).isNotNull();
        List<LocalSubscriptionApi> filtered = filterOutTearDownSubscriptions(localSubscriptions.getSubscritions());
        assertThat(filtered).isNotNull().hasSize(1);
        LocalSubscriptionApi idSubToDelete = filtered.get(0);

        client.deleteSubscriptions(idSubToDelete.getId());
		LocalSubscriptionListApi afterDelete = client.getServiceProviderSubscription();
		List<LocalSubscriptionApi> filteredAfterDelete = filterOutTearDownSubscriptions(afterDelete.getSubscritions());
        assertThat(filteredAfterDelete).hasSize(0);
	}

    @Test
    public void addSubscriptionAskForCapabilities() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
		LocalSubscriptionApi addedSubscription = client.addSubscription(new Datex2DataTypeApi("NO"));
        System.out.println(objectMapper.writeValueAsString(addedSubscription));

        LocalDataTypeList capabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(capabilities));

		LocalSubscriptionListApi serviceProviderSubscriptionRequest = client.getServiceProviderSubscription();
		for (LocalSubscriptionApi subscription : serviceProviderSubscriptionRequest.getSubscritions()) {
			System.out.println("deleting subscription " + subscription.getId());
			client.deleteSubscriptions(subscription.getId());
		}
		LocalSubscriptionListApi afterDelete = client.getServiceProviderSubscription();
		assertThat(filterOutTearDownSubscriptions(afterDelete.getSubscritions())).hasSize(0);
    }


    private List<LocalSubscriptionApi> filterOutTearDownSubscriptions(List<LocalSubscriptionApi> subscriptions) {
        return subscriptions.stream().filter(sub -> !sub.getStatus().equals(LocalSubscriptionStatusApi.TEAR_DOWN)).collect(Collectors.toList());
    }

}
