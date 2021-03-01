package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@Testcontainers
public class OnboardRestClientIT extends DockerBaseIT {

	private static Logger log = LoggerFactory.getLogger(OnboardRestClientIT.class);

	@Container
	private static KeysContainer keysContainer = getKeysContainer(OnboardRestClientIT.class,"my_ca","localhost","test1","test2","test3");

    public static Network network = Network.newNetwork();

    @Container
    public static PostgreSQLContainer dbContainer =
            new PostgreSQLContainer<>("postgres:9.6")
                    .withDatabaseName("federation")
                    .withUsername("federation")
                    .withPassword("federation")
                    .withNetwork(network)
                    .withNetworkAliases("database")
                    .dependsOn(keysContainer);


    @Container
    public static GenericContainer onboardServer = createOnboardContainer()
            .withNetwork(network)
            .dependsOn(dbContainer);


    public static GenericContainer createOnboardContainer() {
        GenericContainer container =  new GenericContainer(
                    new ImageFromDockerfile()
                            .withFileFromPath(".",
                                    getProjectRelativePath("onboard-server-app")
                            )
                    )
                    .withFileSystemBind(keysContainer.getKeyFolderOnHost().toString(),"/jks", BindMode.READ_ONLY)
                    .withEnv("KEY_STORE","/jks/localhost.p12")
                    .withEnv("KEY_STORE_PASSWORD","password")
                    .withEnv("TRUST_STORE_PASSWORD","password")
                    .withEnv("TRUST_STORE","/jks/truststore.jks")
                    .withEnv("SERVER_NAME","localhost")
                    .withEnv("SP_CHNL_PORT","8899")
                    .withEnv("POSTGRES_URI","jdbc:postgresql://database/federation")
                    .withEnv("LOG_LEVELS","-Dlogging.level.no.vegvesen.ixn=DEBUG")
                    .waitingFor(Wait.forLogMessage(".*- Started OnboardApplication in.*",1))
                    .withExposedPorts(8899);
        return container;

    }

    @Test
    public void addCapabilityCheckAndDelete() throws JsonProcessingException {
        SSLContext sslContext = TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(), "test1.p12", "truststore.jks");
        OnboardRESTClient client = new OnboardRESTClient(sslContext,"https://localhost:" + onboardServer.getMappedPort(8899),"test1");;

        DatexCapabilityApi datexNO = new DatexCapabilityApi("NO");
        client.addCapability(datexNO);

        ObjectMapper objectMapper = new ObjectMapper();
        LocalCapabilityList newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));
        assertThat(newCapabilities.getCapabilities()).hasSize(1);

		LocalCapability localDataType = newCapabilities.getCapabilities().iterator().next();
        Integer id = localDataType.getId();
        client.deleteCapability(id);

        newCapabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(newCapabilities));
    }

    @Test
    public void addSubscriptionCheckAndDelete() throws JsonProcessingException {
        SSLContext sslContext = TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(), "test2.p12", "truststore.jks");
        OnboardRESTClient client = new OnboardRESTClient(sslContext,"https://localhost:" + onboardServer.getMappedPort(8899),"test2");;
		client.addSubscription(new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", false));

        LocalSubscriptionListApi localSubscriptions = client.getServiceProviderSubscriptions();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(localSubscriptions));

        assertThat(localSubscriptions).isNotNull();
        List<LocalSubscriptionApi> filtered = filterOutTearDownSubscriptions(localSubscriptions.getSubscriptions());
        assertThat(filtered).isNotNull().hasSize(1);
        LocalSubscriptionApi idSubToDelete = filtered.get(0);

        client.deleteSubscriptions(idSubToDelete.getId());
		LocalSubscriptionListApi afterDelete = client.getServiceProviderSubscriptions();
		List<LocalSubscriptionApi> filteredAfterDelete = filterOutTearDownSubscriptions(afterDelete.getSubscriptions());
        assertThat(filteredAfterDelete).hasSize(0);
	}

    @Test
    public void addSubscriptionAskForCapabilities() throws JsonProcessingException {
        SSLContext sslContext = TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(), "test3.p12", "truststore.jks");
        OnboardRESTClient client = new OnboardRESTClient(sslContext,"https://localhost:" + onboardServer.getMappedPort(8899),"test3");
        ObjectMapper objectMapper = new ObjectMapper();
		LocalSubscriptionApi addedSubscription = client.addSubscription(new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", false));
        System.out.println(objectMapper.writeValueAsString(addedSubscription));

        LocalCapabilityList capabilities = client.getServiceProviderCapabilities();
        System.out.println(objectMapper.writeValueAsString(capabilities));

		LocalSubscriptionListApi serviceProviderSubscriptionRequest = client.getServiceProviderSubscriptions();
		for (LocalSubscriptionApi subscription : serviceProviderSubscriptionRequest.getSubscriptions()) {
			System.out.println("deleting subscription " + subscription.getId());
			client.deleteSubscriptions(subscription.getId());
		}
		LocalSubscriptionListApi afterDelete = client.getServiceProviderSubscriptions();
		assertThat(filterOutTearDownSubscriptions(afterDelete.getSubscriptions())).hasSize(0);
    }


    private List<LocalSubscriptionApi> filterOutTearDownSubscriptions(List<LocalSubscriptionApi> subscriptions) {
        return subscriptions.stream().filter(sub -> !sub.getStatus().equals(LocalSubscriptionStatusApi.TEAR_DOWN)).collect(Collectors.toList());
    }

}
