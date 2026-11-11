package no.vegvesen.ixn.federation;

import jakarta.jms.JMSException;
import jakarta.transaction.Transactional;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {


	private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	public static final String HOST_NAME = getDockerHost();
	private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(ServiceProviderRouterIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.1")
			.withDatabaseName("federation")
			.withUsername("federation")
			.withPassword("federation");

	@Container
    public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
			).withLogConsumer(new Slf4jLogConsumer(logger));


	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		System.out.println("Qpid admin UI: " + qpidContainer.getHttpUrl());
		ClusterKeyGenerator.ClientStore routingConfigurerStore = ClusterKeyGenerator.getClientStore("routing_configurer", stores.clientStores().stream());
		ClusterKeyGenerator.CaStore caStore = stores.trustStore();
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
		registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
		registry.add("routing-configurer.interval",()->"999");
		registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
		registry.add("routing-configurer.vhost",() -> HOST_NAME);
		registry.add("interchange.node-provider.name", () -> HOST_NAME);
		registry.add("spring.ssl.bundle.jks.qpid-client.keystore.location", () -> routingConfigurerStore.path().toString());
		registry.add("spring.ssl.bundle.jks.qpid-client.keystore.password", routingConfigurerStore::password);
		registry.add("spring.ssl.bundle.jks.qpid-client.truststore.location", () -> caStore.truststoreName().toString());
		registry.add("spring.ssl.bundle.jks.qpid-client.truststore.password", caStore::truststorePassword);
	}

	@Autowired
	QpidClient client;

	@Autowired
	private PrivateChannelRepository privateChannelRepository;

	@Autowired
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	private OutgoingMatchRepository outgoingMatchRepository;

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private NeighbourRepository neighbourRepository;


 	@Autowired
	ServiceProviderRouter router;


	@Test
	public void setUpQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider(randomServiceProviderName());
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
		String peerName = randomPeerName();
		PrivateChannel privateChannel = new PrivateChannel(Set.of(new Peer(peerName)), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,serviceProvider.getName());

		privateChannelRepository.save(privateChannel);
		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isTrue();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNotNull();
		assertThat(privateChannel.getPeers()).hasSize(1).allMatch(peer -> peerName.equals(peer.getName()));
	}


	@Test
	public void tearDownQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider(randomServiceProviderName());
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
		PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer(randomPeerName())), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,serviceProvider.getName());

		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isFalse();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNull();
		for (Peer peer : privateChannel.getPeers()) {
			assertThat(client.getPrivateChannelGroupMember(peer.getName())).isNull();
		}
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyAreServiceProviderInAnotherChannel() {
		ServiceProvider serviceProvider = new ServiceProvider(randomServiceProviderName());
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(),randomQueueName());
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(randomPeerName())), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1,serviceProvider.getName());
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(randomPeerName())), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, serviceProvider.getName());

		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel1);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNotNull();
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyArePeerInAnotherChannel(){
        ServiceProvider serviceProvider1 = new ServiceProvider(randomServiceProviderName());
        ServiceProvider serviceProvider2 = new ServiceProvider(randomServiceProviderName());
        PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME,  qpidContainer.getAmqpsPort(), randomQueueName());

        PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(randomPeerName())), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, serviceProvider1.getName());
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider1.getName())), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, serviceProvider2.getName());

		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel1);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();
	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyAreServiceProviderInAnotherChannel(){
        ServiceProvider serviceProvider1 = new ServiceProvider(randomServiceProviderName());
        ServiceProvider serviceProvider2 = new ServiceProvider(randomServiceProviderName());
        PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider2.getName())), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, serviceProvider1.getName());
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider1.getName())), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, serviceProvider2.getName());

		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);

		privateChannel2.setStatus(PrivateChannelStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider2.getName())).isNotNull();

	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyArePeerInAnotherChannel(){
        ServiceProvider serviceProvider1 = new ServiceProvider(randomServiceProviderName());
        ServiceProvider serviceProvider2 = new ServiceProvider(randomServiceProviderName());

        PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint3 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider2.getName())),PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, serviceProvider1.getName());
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider2.getName())), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, serviceProvider1.getName());
		PrivateChannel privateChannel3 = new PrivateChannel(Collections.singleton(new Peer(serviceProvider1.getName())), PrivateChannelStatus.REQUESTED, "my-channel-3", endpoint3, serviceProvider2.getName());

		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);
		privateChannelRepository.save(privateChannel3);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel1);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider2.getName())).isNotNull();

	}

	@Test
	public void addPeerToPrivateChannelAfterCreation() {
        ServiceProvider serviceProvider = new ServiceProvider(randomServiceProviderName());
        PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(HOST_NAME,qpidContainer.getAmqpsPort(), randomQueueName());
        Peer peer = new Peer(randomPeerName());
		PrivateChannel privateChannel = new PrivateChannel(Set.of(peer), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, serviceProvider.getName());

		serviceProviderRepository.save(serviceProvider);
		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember(peer.getName())).isNotNull();

        Peer newPeer = new Peer(randomPeerName());
		privateChannel.addPeer(newPeer);

		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel.getPeers()).allMatch( p -> p.getStatus().equals(PeerStatus.CREATED));
		assertThat(client.getPrivateChannelGroupMember(newPeer.getName())).isNotNull();
	}

	@Test
	public void removePeerToPrivateChannelAfterCreation() {
		String name = randomServiceProviderName();
		ServiceProvider serviceProvider = new ServiceProvider(name);
		String queueName = randomQueueName();
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), queueName);
		Peer peer1 = new Peer("peer-1");
		Peer peer2 = new Peer("peer-2");
		PrivateChannel privateChannel = new PrivateChannel(Set.of(peer1, peer2), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, serviceProvider.getName());
		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("peer-1")).isNotNull();
		assertThat(client.getPrivateChannelGroupMember("peer-2")).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(privateChannel.getPeers()).hasSize(1);
		assertThat(client.getPrivateChannelGroupMember("peer-2")).isNull();

	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenPeerInMultipleChannels() {
		String name = randomServiceProviderName();
		ServiceProvider serviceProvider = new ServiceProvider(name);
        PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
		String peer = randomPeerName();
		Peer peer1 = new Peer(peer);
		Peer peer2 = new Peer(peer);

		PrivateChannel privateChannel1 = new PrivateChannel(Set.of(peer1), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, name);
		PrivateChannel privateChannel2 = new PrivateChannel(Set.of(peer2), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, name);
		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember(peer)).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel1.getPeers()).hasSize(1);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember(peer)).isNotNull();
	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenServiceProviderInOtherChannel() {
        ServiceProvider serviceProvider1 = new ServiceProvider(randomServiceProviderName());
		String name1 = randomServiceProviderName();
		ServiceProvider serviceProvider2 = new ServiceProvider(name1);
        PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
        PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint(HOST_NAME, qpidContainer.getAmqpsPort(), randomQueueName());
		String peer = randomPeerName();
		Peer peer1 = new Peer(peer);
		Peer peer2 = new Peer(serviceProvider1.getName());

		PrivateChannel privateChannel1 = new PrivateChannel(Set.of(peer1), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, serviceProvider1.getName());
		PrivateChannel privateChannel2 = new PrivateChannel(Set.of(peer2), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, serviceProvider2.getName());

		privateChannelRepository.save(privateChannel1);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();
		assertThat(client.getPrivateChannelGroupMember(peer)).isNotNull();

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember(serviceProvider2.getName())).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);
		privateChannelRepository.save(privateChannel2);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();

	}


	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribeFederatedInterchangesGroup() {
		String serviceProviderName = randomServiceProviderName();

		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"a=b",
				"my-node"
		);

		ServiceProvider toreDownServiceProvider = new ServiceProvider(
				serviceProviderName,
				Set.of(localSubscription)
		);

		serviceProviderRepository.save(toreDownServiceProvider);
		router.syncServiceProviders(List.of(toreDownServiceProvider), client.getQpidDelta());
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(toreDownServiceProvider.getName())).isNotNull();
		assertThat(localSubscription.getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);

		Set<LocalEndpoint> localEndpoints = toreDownServiceProvider.getSubscriptions().stream()
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(localEndpoints).hasSize(1);
		LocalEndpoint endpoint = localEndpoints.stream().findFirst().get();

		assertThat(client.queueExists(endpoint.getSource())).isTrue();

		toreDownServiceProvider.getSubscriptions().forEach(s -> s.setStatus(LocalSubscriptionStatus.TEAR_DOWN));

		serviceProviderRepository.save(toreDownServiceProvider);
		router.syncServiceProviders(List.of(toreDownServiceProvider), client.getQpidDelta());
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(toreDownServiceProvider.getName())).isNull();
		assertThat(client.queueExists(endpoint.getSource())).isFalse();
	}

	@Test
	public void serviceProviderShouldBeRemovedWhenCapabilitiesAreRemoved() {
		Capabilities capabilities = new Capabilities(
				Collections.singleton(
						new Capability(
								new DatexApplication("NO-123",
										"NO-pub",
										"NO",
										"1.0",
										List.of(),
										"SituationPublication",
										"publisherName"
								),
								new Metadata(
										RedirectStatus.OPTIONAL
								)
						)
				)
		);
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",capabilities);

		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());

		assertThat(client.getQpidDelta().findServiceProviderMemberByName(serviceProvider.getName())).isNotNull();

		serviceProvider.setCapabilities(new Capabilities());
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(serviceProvider.getName())).isNull();
	}

	@Test
	public void shardedCapabilityGetsEqualNumberOfShardsAsShardCount() {

		Capability cap = new Capability(
				new DatexApplication("NO-123",
						"NO-pub",
						"NO",
						"1.0",
						List.of(),
						"SituationPublication",
						"publisherName"),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap.getMetadata().setShardCount(3);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap)
		);
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",capabilities);

		router.setUpCapabilityExchanges(serviceProvider, client.getQpidDelta());
		assertThat(cap.getStatus()).isEqualTo(CapabilityStatus.CREATED);
		assertThat(cap.isSharded()).isTrue();
		assertThat(cap.hasShards()).isTrue();
		assertThat(cap.getShards()).hasSize(3);
		for (CapabilityShard shard : cap.getShards()) {
			assertThat(client.exchangeExists(shard.getExchangeName())).isTrue();
			assertThat(shard.getSelector().contains("shardId")).isTrue();
		}
	}

	@Test
	public void nonShardedCapabilityIsSetUp() {

		Capability cap = new Capability(
				new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptyList(), "SituationPublication", "publisherName"),
				new Metadata(RedirectStatus.OPTIONAL)
		);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap));
		ServiceProvider serviceProvider = new ServiceProvider(randomServiceProviderName(),capabilities);

		router.setUpCapabilityExchanges(serviceProvider, client.getQpidDelta());
		assertThat(cap.getStatus()).isEqualTo(CapabilityStatus.CREATED);
		assertThat(cap.isSharded()).isFalse();
		assertThat(cap.hasShards()).isTrue();
		assertThat(cap.getShards()).hasSize(1);
		assertThat(client.exchangeExists(cap.getShards().stream().findFirst().get().getExchangeName())).isTrue();
		assertThat(cap.getShards().stream().findFirst().orElseThrow().getSelector().contains("shardId")).isFalse();
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasConsumerCommonNameSameAsIxnNameAndServiceProviderName() {
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'",
				"my-node");
		String consumerCommonName = randomServiceProviderName();
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'",
				consumerCommonName);

		ServiceProvider serviceProvider = new ServiceProvider(consumerCommonName,Set.of(sub1,sub2));

		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(serviceProvider.getName())).isNotNull();

		assertThat(sub1.getLocalEndpoints()).hasSize(1);
		LocalEndpoint endpoint1 = sub1.getLocalEndpoints().stream().findFirst().get();
		assertThat(client.queueExists(endpoint1.getSource())).isTrue();

		assertThat(sub2.getLocalEndpoints()).hasSize(0);
	}

	@Test
	public void serviceProviderShouldBeRemovedFromGroupWhenTheyHaveNoCapabilitiesOrSubscriptions() {
		Capabilities capabilities = new Capabilities(
				Collections.singleton(new Capability(new DatexApplication("NO-123", "NO-pub","NO", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL))));
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider-should-be-removed",capabilities);

		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(serviceProvider.getName())).isNotNull();

		serviceProvider.setCapabilities(new Capabilities());
		serviceProviderRepository.save(serviceProvider);

		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());
		assertThat(client.getQpidDelta().findServiceProviderMemberByName(serviceProvider.getName())).isNull();
	}




	@Test
	public void tearDownTargetForDeliveryByDeletedDelivery() {
		String serviceProviderName = randomServiceProviderName();
		String exchangeName = "my-exchange8";

		CapabilityShard shard = new CapabilityShard(1, "cap-ex4", "publicationId = 'pub-1'");
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard)
		);
		client.createHeadersExchange("cap-ex4");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery", false);
		delivery.addEndpoint(new LocalDeliveryEndpoint(HOST_NAME,  qpidContainer.getAmqpsPort(), exchangeName));
		ServiceProvider serviceProvider = new ServiceProvider(
				serviceProviderName,
				new Capabilities(Set.of(denmCapability)),
				Set.of(),
				Set.of(delivery),
				LocalDateTime.now()
		);

		serviceProviderRepository.save(serviceProvider);
		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
		outgoingMatchRepository.save(match);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		outgoingMatchRepository.delete(match);
		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);
		serviceProviderRepository.save(serviceProvider);

		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.getEndpoints()).isEmpty();
	}


	@Test
	public void tearDownDlqForDeliveryByDeletedDelivery() {
		String serviceProviderName = randomServiceProviderName();
		String deliveryExchange = "dlq-exchange";
		String dlqName = "dlq-name";

        Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				List.of(
						new CapabilityShard(
								1,
								"cap-ex40",
								"publicationId = 'pub-1'"
						)
				)
		);
		client.createHeadersExchange("cap-ex40");

		LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(
				"host",
				123,
				deliveryExchange,
				2,
				3,
				dlqName);
		LocalDelivery delivery = new LocalDelivery(
				UUID.randomUUID().toString(),
				Set.of(endpoint),
				"originatingCountry = 'NO'",
				LocalDeliveryStatus.CREATED,
				"delivery",
				false
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				serviceProviderName,
				new Capabilities(
						Set.of(
								denmCapability
						)
				),
				Set.of(),
				Set.of(delivery),
				LocalDateTime.now()
		);

		serviceProviderRepository.save(serviceProvider);
		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
		outgoingMatchRepository.save(match);

		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);
		serviceProviderRepository.save(serviceProvider);
		outgoingMatchRepository.delete(match);

		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.queueExists(dlqName)).isFalse();
		assertThat(client.exchangeExists(deliveryExchange)).isFalse();
		assertThat(delivery.getEndpoints()).isEmpty();
	}


	@Test
	public void connectionGetsRemovedWhenCapabilityIsRemoved(){

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-3", HOST_NAME,  qpidContainer.getAmqpsPort());
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-3");

		CapabilityShard shard = new CapabilityShard(1, "cap-ex15", "publicationId = 'pub-1'");
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard)
		);
		client.createHeadersExchange("cap-ex15");

		denmCapability.setStatus(CapabilityStatus.CREATED);

		ServiceProvider mySP = new ServiceProvider("my-sp",Set.of(subscription));
        ServiceProvider otherSP = new ServiceProvider("other-sp", new Capabilities(Collections.singleton(denmCapability)));
		serviceProviderRepository.save(mySP);
		serviceProviderRepository.save(otherSP);
		router.syncServiceProviders(List.of(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().orElseThrow().getSource())).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);
		serviceProviderRepository.save(otherSP);

		router.syncServiceProviders(List.of(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().orElseThrow().getSource())).hasSize(0);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(0);
	}

	@Test
	public void localSubscriptionKeepsConnectionToOneCapabilityAndTearsDownAnother() {
		String endpointName = "endpoint-4";
        LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.CREATED,
				"originatingCountry = 'NO'",
				"my-node",
				Set.of(),
				Set.of(new LocalEndpoint(endpointName, HOST_NAME,  qpidContainer.getAmqpsPort()))
		);
		client.createQueue(endpointName);

		String capabilityExchange = "cap-ex16";
		Capability denmCapability1 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(new CapabilityShard(1, capabilityExchange, "publicationId = 'pub-1'"))
		);
		denmCapability1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange(capabilityExchange);

		String capExchangeName2 = "cap-ex17";
		Capability denmCapability2 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-2",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(new CapabilityShard(1, capExchangeName2, "publicationId = 'pub-2'"))
		);
		denmCapability2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange(capExchangeName2);


		ServiceProvider mySP = new ServiceProvider(
				"my-sp",
				Set.of(subscription));
        ServiceProvider otherSP = new ServiceProvider(
				"other-sp",
				new Capabilities(
						Set.of(
								denmCapability1,
								denmCapability2
						)
				)
		);

		serviceProviderRepository.save(mySP);
		serviceProviderRepository.save(otherSP);
		router.syncServiceProviders(List.of(mySP, otherSP), client.getQpidDelta());

		LocalEndpoint localEndpoint = subscription.getLocalEndpoints().stream().findFirst().orElseThrow();
		assertThat(localEndpoint.getSource()).isEqualTo(endpointName);
		List<Binding> queuePublishingLinks = client.getQueuePublishingLinks(endpointName);
		assertThat(queuePublishingLinks).hasSize(2);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(2);

		denmCapability1.setStatus(CapabilityStatus.TEAR_DOWN);

		serviceProviderRepository.save(otherSP);
		router.syncServiceProviders(List.of(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);
	}

	@Test
	public void routerPicksUpRequestedLocalSubscription() {
		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
				"a.bouvetinterchange.eu"

		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				new Capabilities(),
				Collections.singleton(
						localSubscription
				),
				Set.of(),
				LocalDateTime.now()
		);

		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Set.of(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions().stream().findFirst().get().getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);
	}

	@Test
	public void testNoServiceProviderIsAddedToBiConsumerGroupIfBiconsumerIsFalse() {

		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				false,
				new Capabilities(),
				Set.of(),
				Set.of(),
				LocalDateTime.now()
		);
		serviceProviderRepository.save(serviceProvider);
		assertThat(serviceProvider.isBiconsumer()).isFalse();
		router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider, client.getQpidDelta());

		assertThat(client.getBiConsumerMember(serviceProvider.getName())).isNull();
	}


    @Test
    public void testNoServiceProviderIsAddedToBiConsumerGroupIfBiconsumerIsNull() {

        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                null,
                new Capabilities(),
                Set.of(),
                Set.of(),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);
        router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider,client.getQpidDelta());

        assertThat(client.getBiConsumerMember(serviceProvider.getName())).isNull();
    }


	@Test
	public void testServiceProviderAddedToBiConsumerGroup() {

		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				true,
				new Capabilities(),
				Set.of(),
				Set.of(),
				LocalDateTime.now()
		);
		serviceProviderRepository.save(serviceProvider);
		assertThat(serviceProvider.isBiconsumer()).isTrue();
		router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider,client.getQpidDelta());

		assertThat(client.getBiConsumerMember(serviceProvider.getName()).name()).isEqualTo(serviceProvider.getName());
	}



	@Test
	public void testIllegalLocalSubscriptionGetsRemovedFromServiceProvider() {
		LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Set.of(subscription)
		);
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void testLocalSubscriptionWithErrorGetsRemovedFromServiceProvider(){
		LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.ERROR,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Set.of(subscription)
		);
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void tearDownLocalSubscriptionWithEmptyMatch() {
		LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Set.of(subscription)
		);
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(List.of(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void teardownLocalSubscriptionWithRemainingMatch() {
		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(localSubscription)
		);
		Subscription subscription = new Subscription("", SubscriptionStatus.TEAR_DOWN);
		Neighbour neighbour = new Neighbour(
			"neighbour",
			new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(Set.of(subscription))
		);

		Match match = new Match(
				localSubscription,
				subscription
		);

		serviceProviderRepository.save(serviceProvider);
		neighbourRepository.save(neighbour);
		matchRepository.save(match);
		router.syncServiceProviders(Set.of(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
	}

	@Test
	public void redirectSubscriptionStatusTearDownEmptyMatchList() {
		LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);

		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Set.of(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void redirectSubscriptionStatusIllegal() {
		LocalSubscription subscription = new LocalSubscription(
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"sp1"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void bindNonExistingCapabilityExchangeToBiQueue() {
		Queue queue = client.getQueue("bi-datex");
		assertThat(queue).isNotNull();

		String name = randomServiceProviderName();
		ServiceProvider serviceProvider = new ServiceProvider(
				name,
				new Capabilities(
						Set.of(
								new Capability(
										"123-323",
										new DatexApplication(
												"NO12345",
												"NO12345:001",
												"NO",
												"1.0",
												List.of("1234"),
												"type",
												"publisher"
										),
										new Metadata(
												"https://mysite.com",
												1,
												RedirectStatus.OPTIONAL,
												0,
												0,
												0
										),
										List.of(
												new CapabilityShard(
														1,
														"this-exchange-does-not-exist-shard-1",
														"publicationId = 'NO12345:001' and shardId = 1"
												)
										)
								)
						)
				),
				Set.of(),
				Set.of(),
				null
		);

		assertThatNoException().isThrownBy( () -> router.bindCapabilityExchangesToBiQueue(serviceProvider,client.getQpidDelta()));
	}

	@Test
	public void testSetupBindingToBiQueue() {
		{
			String biQueueName = "bi-datex";
			Queue queue = client.getQueue(biQueueName);
			assertThat(queue).isNotNull();

			String exchangeName = UUID.randomUUID().toString();
			Exchange headersExchange = client.createHeadersExchange(exchangeName);

			assertThat(headersExchange.isBoundTo(biQueueName)).isFalse();
			String name = randomServiceProviderName();
			ServiceProvider serviceProvider = new ServiceProvider(
					name,
					new Capabilities(
							Set.of(
									new Capability(
											"123-323",
											new DatexApplication(
													"NO12345",
													"NO12345:001",
													"NO",
													"1.0",
													List.of("1234"),
													"type",
													"publisher"
											),
											new Metadata(
													"https://mysite.com",
													1,
													RedirectStatus.OPTIONAL,
													0,
													0,
													0
											),
											List.of(
													new CapabilityShard(
															1,
															exchangeName,
															"publicationId = 'NO12345:001' and shardId = 1"
													)
											)
									)
							)
					),
					Set.of(),
					Set.of(),
					null
			);
			router.bindCapabilityExchangesToBiQueue(serviceProvider,client.getQpidDelta());
			Exchange exchange = client.getExchange(exchangeName);
			assertThat(exchange).isNotNull();
			assertThat(exchange.isBoundTo(biQueueName)).isTrue();
		}
	}

	private static String randomServiceProviderName() {
		return "service-provider-" + UUID.randomUUID();
	}

	private static String randomPeerName() {
		return "peer-" + UUID.randomUUID();
	}

	private static String randomQueueName() {
		return "queue-" + UUID.randomUUID();
	}

	private static String randomExchangeName() {
		return "exchange-" + UUID.randomUUID();
	}
}
