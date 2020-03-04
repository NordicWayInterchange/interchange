package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {QpidClientIT.Initializer.class})
public class QpidClientIT extends DockerBaseIT {

	@ClassRule
	public static GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private static Logger logger = LoggerFactory.getLogger(QpidClientIT.class);
	private static Integer MAPPED_HTTPS_PORT;

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = "https://localhost:" + qpidContainer.getMappedPort(HTTPS_PORT);
			String httpUrl = "http://localhost:" + qpidContainer.getMappedPort(8080);
			logger.info("server url: " + httpsUrl);
			logger.info("server url: " + httpUrl);
			TestPropertyValues.of(
					"qpid.rest.api.baseUrl=" + httpsUrl,
					"qpid.rest.api.vhost=localhost"
			).applyTo(configurableApplicationContext.getEnvironment());
			MAPPED_HTTPS_PORT = qpidContainer.getMappedPort(HTTPS_PORT);
		}
	}

	@Autowired
	QpidClient client;

	@Autowired
	RestTemplate restTemplate;

	@Test
	public void pingQpid() {
		assertThat(client.ping()).isEqualTo(200);
	}

	@Test
	public void createQueue() {
		client.createQueue("findus");
	}

	@Test(expected = Exception.class)
	public void createQueueWithIllegalCharactersInIdFails() {
		client._createQueue("torsk");
		client._createQueue("torsk"); //create some queue that already exists
	}

	@Test
	public void createdQueueCanBeQueriedFromQpid() {
		client.createQueue("leroy");
		assertThat(client.queueExists("leroy")).isTrue();
	}

	@Test
	public void queueNotCreatedQueueDoesNotExist() {
		assertThat(client.queueExists("mackrel")).isFalse();
	}

	@Test
	public void tearDownQueue() {
		//Set up a new queue
		client.createQueue("crab");
		assertThat(client.queueExists("crab")).isTrue();

		//Delete the queue
		client.removeQueue("crab");
		assertThat(client.queueExists("crab")).isFalse();
	}

	@Test
	public void addAnInterchangeToGroups() {
		String newUser = "herring";
		client.addMemberToGroup(newUser, FEDERATED_GROUP_NAME);

		List<String> userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);

		assertThat(userNames).contains(newUser);
	}

	@Test
	public void deleteAnInterchangeFromGroups() {
		String deleteUser = "carp";
		client.addMemberToGroup(deleteUser, FEDERATED_GROUP_NAME);
		List<String> userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).contains(deleteUser);

		client.removeMemberFromGroup(FEDERATED_GROUP_NAME, deleteUser);
		userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).doesNotContain(deleteUser);
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}

	@Test
	public void addAccessBuildsUpRules() {
		List<String> initialACL = client.getACL();
		client.addReadAccess("routing_configurer", "onramp");
		List<String> newACL = client.getACL();
		assertThat(newACL).hasSize(initialACL.size() + 1);
	}

	@Test
	public void addOneConsumeRuleBeforeLastRule() {
		LinkedList<String> acl = new LinkedList<>();
		acl.add("ACL ALLOW-LOG interchange ALL ALL");
		acl.add("ACL ALLOW-LOG administrators ALL ALL");
		acl.add("ACL ALLOW-LOG " + SERVICE_PROVIDERS_GROUP_NAME + " PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"");
		acl.add("ACL ALLOW-LOG " + SERVICE_PROVIDERS_GROUP_NAME + " ACCESS VIRTUALHOST name = \"localhost\"");
		acl.add("ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"");
		acl.add("ACL DENY-LOG ALL ALL ALL");
		LinkedList<String> newAcl = new LinkedList<>(client.addOneConsumeRuleBeforeLastRule("king_harald", "king_harald", acl));
		assertThat(acl.getFirst()).isEqualTo(newAcl.getFirst());
		assertThat(acl.getLast()).isEqualTo(newAcl.getLast());
		assertThat(newAcl.get(newAcl.size()-2)).contains("king_harald");
	}

	@Test
	public void httpsConnectionToQpidRestServerInsideTheClusterDoesNotVerifyServerName() {
		QpidClient localhostAddressedWithIpAddress = new QpidClient("https://127.0.0.1:" + MAPPED_HTTPS_PORT, "localhost", restTemplate);
		localhostAddressedWithIpAddress.ping();
	}
}