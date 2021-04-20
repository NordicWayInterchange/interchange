package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static no.vegvesen.ixn.federation.qpid.QpidClient.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {QpidClientIT.Initializer.class})
@Testcontainers
public class QpidClientIT extends QpidDockerBaseIT {

	private static Path testKeysPath = getFolderPath("target/test-keys" + QpidClientIT.class.getSimpleName());

	@Container
	private static GenericContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "testhost", "routing_configurer");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "testhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

	private static Logger logger = LoggerFactory.getLogger(QpidClientIT.class);

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = qpidContainer.getHttpsUrl();
			logger.info("server url: " + httpsUrl);
			TestPropertyValues.of(
					"routing-configurer.baseUrl=" + httpsUrl,
					"routing-configurer.vhost=localhost",
					"test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
					"test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12")
			).applyTo(configurableApplicationContext.getEnvironment());
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

	@Test
	public void createQueueWithIllegalCharactersInIdFails() {
		client._createQueue("torsk");

		assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
			client._createQueue("torsk"); //create some queue that already exists
			//
		});
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

		client.removeMemberFromGroup(deleteUser, FEDERATED_GROUP_NAME);
		userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).doesNotContain(deleteUser);
	}

	@Test
	public void addRemoteServiceProviderToGroup() {
		String newUser = "service-provider";
		client.addMemberToGroup(newUser, REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		List<String> userNames = client.getGroupMemberNames(REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(userNames).contains(newUser);
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
		String newAclEntry = String.format("ACL ALLOW-LOG king_harald CONSUME QUEUE name = \"king_harald\"");
		LinkedList<String> newAcl = new LinkedList<>(client.addOneConsumeRuleBeforeLastRule(acl, newAclEntry));
		assertThat(acl.getFirst()).isEqualTo(newAcl.getFirst());
		assertThat(acl.getLast()).isEqualTo(newAcl.getLast());
		assertThat(newAcl.get(newAcl.size() - 2)).contains("king_harald");
	}

	@Test
	public void readAccessIsAdded() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.addReadAccess(subscriberName, queueName);

		List<String> newAclRules = client.getACL();

		String newAclEntry = String.format("ACL ALLOW-LOG king_harald CONSUME QUEUE name = \"king_harald\"");

		assertThat(newAclRules.get(newAclRules.size() - 2)).isEqualTo(newAclEntry);
	}

	@Test
	public void readAccessIsRemoved() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.removeReadAccess(subscriberName, queueName);

		List<String> newAclRules = client.getACL();

		String deletedAclEntry = String.format("ACL ALLOW-LOG king_harald CONSUME QUEUE name = \"king_harald\"");

		assertThat(newAclRules).doesNotContain(deletedAclEntry);
	}

	@Test
	public void writeAccessIsAdded() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.addWriteAccess(subscriberName, queueName);

		List<String> newAclRules = client.getACL();

		assertThat(writeAccessIsInAclRules(newAclRules, subscriberName, queueName)).isTrue();
	}

	@Test
	public void writeAccessIsRemoved() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.removeWriteAccess(subscriberName, queueName);

		List<String> newAclRules = client.getACL();

		assertThat(writeAccessIsInAclRules(newAclRules, subscriberName, queueName)).isFalse();
	}

	public boolean writeAccessIsInAclRules(List<String> aclRules, String subscriberName, String queueName) {
		for (String rule : aclRules) {
			if(rule.startsWith(String.format("ACL ALLOW-LOG %s PUBLISH EXCHANGE", subscriberName)) &&
				rule.contains("name = \"\"") &&
				rule.contains(String.format("routingkey = \"%s\"", queueName))){
				return true;
			}
		}
		return false;
	}

	@Test
	public void httpsConnectionToQpidRestServerInsideTheClusterDoesNotVerifyServerName() {
		QpidClient localhostAddressedWithIpAddress = new QpidClient("https://127.0.0.1:" + qpidContainer.getHttpsUrl(), "localhost", restTemplate);
		localhostAddressedWithIpAddress.ping();
	}

	@Test
	public void removeServiceProviderFromGroup() {
		String myUser = "my-service-provider";
		client.addMemberToGroup(myUser, SERVICE_PROVIDERS_GROUP_NAME);
		List<String> myUserNames = client.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(myUserNames).contains(myUser);

		client.removeMemberFromGroup(myUser, SERVICE_PROVIDERS_GROUP_NAME);
		myUserNames = client.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(myUserNames).doesNotContain(myUser);
	}
}