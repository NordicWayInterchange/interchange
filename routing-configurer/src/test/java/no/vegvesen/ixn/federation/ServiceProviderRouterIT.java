package no.vegvesen.ixn.federation;

import jakarta.jms.JMSException;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {ServiceProviderRouter.class, QpidClient.class, QpidClientConfig.class, InterchangeNodeProperties.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
public class ServiceProviderRouterIT extends QpidDockerBaseIT {

	private static final Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	public static final String HOST_NAME = getDockerHost();

	private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(ServiceProviderRouterIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
			);

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		qpidContainer.followOutput(new Slf4jLogConsumer(logger));
		registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
		registry.add("routing-configurer.vhost", () -> "localhost");
		registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
		registry.add("test.ssl.key-store", () -> getClientStorePath("routing_configurer", stores.clientStores()));
		registry.add("interchange.node-provider.name", () -> HOST_NAME);
	}

	@BeforeAll
	static void setup(){
		qpidContainer.start();
	}

	@MockBean
	ServiceProviderRepository serviceProviderRepository;

	@MockBean
	PrivateChannelRepository privateChannelRepository;

	@Autowired
	QpidClient client;

 	@Autowired
	ServiceProviderRouter router;

	@MockBean
	MatchRepository matchRepository;

	@MockBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockBean
	OutgoingMatchRepository outgoingMatchRepository;

	@Test
	public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
		ServiceProvider nordea = new ServiceProvider("nordea");
		LocalSubscription localSubscription1 = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2' and originatingCountry = 'NO'",
				HOST_NAME
		);
		Capability capability = new Capability(
				new DatexApplication("NO12345", "NO12345:1", "NO", "DATEX2:1.1", List.of("1"), "roadworks", "name"),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability.getShards().add(new CapabilityShard(1, "nordea", MessageValidatingSelectorCreator.makeSelector(capability, 1)));
		capability.setStatus(CapabilityStatus.REQUESTED);
		nordea.addLocalSubscription(localSubscription1);
		nordea.getCapabilities().addCapability(capability);
		when(serviceProviderRepository.save(any())).thenReturn(nordea);router.syncServiceProviders(List.of(nordea), client.getQpidDelta());
		router.syncServiceProviders(List.of(nordea), client.getQpidDelta());
		Set<LocalEndpoint> endpoints = nordea.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(endpoints).hasSize(1);

		LocalSubscription localSubscription2 = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2' and originatingCountry = 'FI'",
				HOST_NAME
		);

		nordea.addLocalSubscription(localSubscription2);
		Capability capability2 = new Capability(
				new DatexApplication("FI12345", "FI12345:1", "FI", "DATEX2:1.1", List.of("1"), "roadworks", "name"),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability2.getShards().add(new CapabilityShard(1, "nordea", MessageValidatingSelectorCreator.makeSelector(capability2, 1)));
		capability2.setStatus(CapabilityStatus.REQUESTED);
		nordea.getCapabilities().addCapability(capability2);
		router.syncServiceProviders(List.of(nordea), client.getQpidDelta());
		Set<LocalEndpoint> endpoints2 = nordea.getSubscriptions().stream()
				.filter(s -> s.getSelector().contains("'FI'"))
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(endpoints2).hasSize(1);
	}

	@Test
	public void setUpQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, "queueName");
		PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer("peer")), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,"service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isTrue();
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		for (Peer peer : privateChannel.getPeers()) {
			assertThat(client.getGroupMember(peer.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		}

		verify(privateChannelRepository, times(1)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(1)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void tearDownQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, "queueName");
		PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer("peer")), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,"service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isFalse();
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();
		for (Peer peer : privateChannel.getPeers()) {
			assertThat(client.getGroupMember(peer.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();
		}

		verify(privateChannelRepository, times(2)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyAreServiceProviderInAnotherChannel() {
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange", 5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("peer-1")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1,"service-provider");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("peer-2")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(List.of(privateChannel2));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.emptyList(),Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(1L,0L);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyArePeerInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-provider-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-provider-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("peer-1")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1,"service-provider-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-provider-1")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-provider-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel2), Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(0L,0L);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider1.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyAreServiceProviderInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("service-2")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "service-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-1")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());
		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);

		privateChannel2.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel1), Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(), any())).thenReturn(0L, 1L);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider1.getName(), QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember(serviceProvider2.getName(), QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyArePeerInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-2");

		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");
		PrivateChannelEndpoint endpoint3 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-3");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("service-2")),PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "service-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-2")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-1");
		PrivateChannel privateChannel3 = new PrivateChannel(Collections.singleton(new Peer("service-1")), PrivateChannelStatus.REQUESTED, "my-channel-3", endpoint3, "service-2");

		when(privateChannelRepository.findAllByServiceProviderName(serviceProvider1.getName())).thenReturn(List.of(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByServiceProviderName(serviceProvider2.getName())).thenReturn(List.of(privateChannel3));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByPeerNameAndStatus(any(),any())).thenReturn(Collections.emptyList(),Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(1L,1L);
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(List.of(privateChannel2));

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider2.getName(), QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void addPeerToPrivateChannelAfterCreation() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange",5671,"queueName");
		Peer peer = new Peer("peer");
		PrivateChannel privateChannel = new PrivateChannel(new HashSet<>(Arrays.asList(peer)), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("peer", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		Peer newPeer = new Peer("new-peer");
		privateChannel.addPeer(newPeer);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.singletonList(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(newPeer.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("new-peer", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(2)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(0)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(0)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreation() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange",5671,"queueName");
		Peer peer1 = new Peer("peer-1");
		Peer peer2 = new Peer("peer-2");
		PrivateChannel privateChannel = new PrivateChannel(new HashSet<>(Arrays.asList(peer1, peer2)), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("peer-1", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember("peer-2", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(privateChannel.getPeers()).hasSize(1);
		assertThat(client.getGroupMember("peer-2", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();

		verify(privateChannelRepository, times(2)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenPeerInMultipleChannels() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-2");
		Peer peer1 = new Peer("peer");
		Peer peer2 = new Peer("peer");

		PrivateChannel privateChannel1 = new PrivateChannel(new HashSet<>(Arrays.asList(peer1)), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "my-service-provider");
		PrivateChannel privateChannel2 = new PrivateChannel(new HashSet<>(Arrays.asList(peer2)), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("peer", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel1.getPeers()).hasSize(1);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("peer", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(3)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenServiceProviderInOtherChannel() {
		ServiceProvider serviceProvider1 = new ServiceProvider("my-service-provider-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("my-service-provider-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-2");
		Peer peer1 = new Peer("peer");
		Peer peer2 = new Peer("my-service-provider-1");

		PrivateChannel privateChannel1 = new PrivateChannel(new HashSet<>(Arrays.asList(peer1)), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "my-service-provider-1");
		PrivateChannel privateChannel2 = new PrivateChannel(new HashSet<>(Arrays.asList(peer2)), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "my-service-provider-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("my-service-provider-1", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember("peer", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();


		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getGroupMember("my-service-provider-2", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(), any())).thenReturn(1L);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(client.getGroupMember("my-service-provider-1", QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();

		verify(privateChannelRepository, times(3)).save(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removeSubscriptionWhenSelectorIsInvalid(){
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		when(serviceProviderRepository.save(king_gustaf)).thenReturn(king_gustaf);

		king_gustaf.addLocalSubscription(new LocalSubscription(
				1,
				LocalSubscriptionStatus.ERROR,
				"1=1",
				HOST_NAME,
				Collections.emptySet(),
				Set.of()
		));

		king_gustaf.addLocalSubscription(new LocalSubscription(
				2,
				LocalSubscriptionStatus.ERROR,
				"messageType = 'DATEX2'",
				HOST_NAME,
				Collections.emptySet(),
				Set.of()
		));

		king_gustaf.addLocalSubscription(new LocalSubscription(
				3,
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX23'",
				HOST_NAME,
				Collections.emptySet(),
				Set.of()
		));
		router.syncServiceProviders(List.of(king_gustaf), client.getQpidDelta());
		router.removeUnwantedSubscriptions(king_gustaf);
		assertThat(king_gustaf.getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		String source = "king_gustaf_source";
		king_gustaf.addLocalSubscription(new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2'",
				HOST_NAME,
				Collections.emptySet(),
				Collections.singleton(new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		));

		Capability capability = new Capability(
				new DatexApplication(
						"NO-123",
						"pub-1",
						"NO",
						"1.0",
						List.of("0122"),
						"publicationType",
						"publisherName"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability.setStatus(CapabilityStatus.CREATED);
		Capabilities capabilities = new Capabilities(
				Collections.singleton(capability
				)
		);
		king_gustaf.setCapabilities(capabilities);
		String deliverySelector = "messageType = 'DATEX2'";
		LocalDelivery localDelivery = new LocalDelivery(
				1,
				deliverySelector,
				LocalDeliveryStatus.CREATED
		);
		String exchangeName = "myexchange";
		localDelivery.addEndpoint(new LocalDeliveryEndpoint(
				qpidContainer.getHost(),
				qpidContainer.getAmqpsPort(),
				exchangeName
		));
		king_gustaf.addDeliveries(Collections.singleton(localDelivery));

		OutgoingMatch outgoingMatch = new OutgoingMatch(
				localDelivery,
				capability,
				king_gustaf.getName()
		);
		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(outgoingMatch));
		when(serviceProviderRepository.save(any())).thenReturn(king_gustaf);
		router.syncServiceProviders(Arrays.asList(king_gustaf), client.getQpidDelta());
		SSLContext kingGustafSslContext = sslClientContext(stores,"king_gustaf");
		String amqpsUrl = qpidContainer.getAmqpsUrl();
		System.out.println(king_gustaf.getCapabilities().getCapabilities());
		Set<LocalEndpoint> sinkEndpoints = king_gustaf.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(sinkEndpoints).hasSize(1);

		LocalEndpoint endpoint = sinkEndpoints.stream().findFirst().get();
		Sink readKingGustafQueue = new Sink(amqpsUrl, endpoint.getSource(), kingGustafSslContext);
		readKingGustafQueue.start();

		Set<LocalDeliveryEndpoint> deliveryEndpoints = king_gustaf.getDeliveries().stream().flatMap(d -> d.getEndpoints().stream()).collect(Collectors.toSet());
		assertThat(deliveryEndpoints).hasSize(1);
		LocalDeliveryEndpoint deliveryEndpoint = deliveryEndpoints.stream().findFirst().get();

		Source writeOnrampQueue = new Source(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on local endpoint");
		} catch (Exception ignore) {
		}
	}

	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribeFederatedInterchangesGroup() {
		String serviceProviderName = "tore-down-service-provider";

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");

		ServiceProvider toreDownServiceProvider = new ServiceProvider(
				serviceProviderName,
				Collections.singleton(localSubscription)
		);

		toreDownServiceProvider.addLocalSubscription(localSubscription);

		when(serviceProviderRepository.save(any())).thenReturn(toreDownServiceProvider);
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(toreDownServiceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(localSubscription.getStatus()).isEqualTo(LocalSubscriptionStatus.NO_OVERLAP);

		Set<LocalEndpoint> localEndpoints = toreDownServiceProvider.getSubscriptions().stream()
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(localEndpoints).hasSize(0);


		toreDownServiceProvider.setSubscriptions(
				toreDownServiceProvider.getSubscriptions().stream()
						.map(localSubscription1 -> localSubscription1.withStatus(LocalSubscriptionStatus.TEAR_DOWN))
						.collect(Collectors.toSet()));

		when(matchRepository.findAllByLocalSubscriptionId(any(Integer.class))).thenReturn(Collections.emptyList());
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getGroupMember(toreDownServiceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
	}

	@Test
	public void serviceProviderShouldBeRemovedWhenCapabilitiesAreRemoved() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider");
		Capabilities capabilities = new Capabilities(
				Collections.singleton(new Capability(new DatexApplication("NO-123", "NO-pub","NO", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL))));
		serviceProvider.setCapabilities(capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());

		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		serviceProvider.setCapabilities(new Capabilities(new HashSet<>()));
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
	}

	@Test
	public void shardedCapabilityGetsEqualNumberOfShardsAsShardCount() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider");

		Capability cap = new Capability(
				new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptyList(), "SituationPublication", "publisherName"),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap.getMetadata().setShardCount(3);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap));
		serviceProvider.setCapabilities(capabilities);

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
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider");

		Capability cap = new Capability(
				new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptyList(), "SituationPublication", "publisherName"),
				new Metadata(RedirectStatus.OPTIONAL)
		);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap));
		serviceProvider.setCapabilities(capabilities);

		router.setUpCapabilityExchanges(serviceProvider, client.getQpidDelta());
		assertThat(cap.getStatus()).isEqualTo(CapabilityStatus.CREATED);
		assertThat(cap.isSharded()).isFalse();
		assertThat(cap.hasShards()).isTrue();
		assertThat(cap.getShards()).hasSize(1);
		assertThat(client.exchangeExists(cap.getShards().stream().findFirst().get().getExchangeName())).isTrue();
		assertThat(cap.getShards().stream().findFirst().get().getSelector().contains("shardId")).isFalse();
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasConsumerCommonNameSameAsIxnNameAndServiceProviderName() {
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'",
				"my-node");
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'",
				"my-service-provider");

		Capability capability = new Capability(
				new DatexApplication("NO12345", "NO12345:1", "NO", "DATEX2:1.1", List.of("01230122", "01230123"), "roadWorks", "name"),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability.setStatus(CapabilityStatus.REQUESTED);
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		serviceProvider.addLocalSubscription(sub1);
		serviceProvider.addLocalSubscription(sub2);
		serviceProvider.getCapabilities().addCapability(capability);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		assertThat(sub1.getLocalEndpoints()).hasSize(1);
		LocalEndpoint endpoint1 = sub1.getLocalEndpoints().stream().findFirst().get();
		assertThat(client.queueExists(endpoint1.getSource())).isTrue();

		assertThat(sub2.getLocalEndpoints()).hasSize(0);
	}

	@Test
	public void serviceProviderShouldBeRemovedFromGroupWhenTheyHaveNoCapabilitiesOrSubscriptions() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider-should-be-removed");
		Capabilities capabilities = new Capabilities(
				Collections.singleton(new Capability(new DatexApplication("NO-123", "NO-pub","NO", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL))));
		serviceProvider.setCapabilities(capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		serviceProvider.setCapabilities(new Capabilities(new HashSet<>()));

		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
	}

	@Test
	public void tearDownQueueWhenLocalSubscriptionIsDeletedAfterMatch() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		String queueName = "my-queue";
		InterchangeNodeProperties nodeProperties = new InterchangeNodeProperties("my-host","1234");
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN, selector, "my-node");
		localSubscription.setLocalEndpoints(Collections.singleton(
				new LocalEndpoint(queueName,
						nodeProperties.getName(),
						Integer.parseInt(nodeProperties.getMessageChannelPort())
				)
		));
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		serviceProvider.addLocalSubscription(localSubscription);

		client.createQueue(queueName);

		when(matchRepository.findAllByLocalSubscriptionId(any(Integer.class))).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncSubscriptions(serviceProvider, client.getQpidDelta());

		assertThat(client.queueExists(queueName)).isFalse();
	}

	@Test
	public void createTargetAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex1", "publicationId = 'pub-1'");
		denmCapability.setShards(Collections.singletonList(shard));

		client.createHeadersExchange("cap-ex1");

		String deliveryExchangeName = "my-exchange5";
		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		verify(serviceProviderRepository, times(1)).save(any());

		assertThat(client.exchangeExists(deliveryExchangeName)).isTrue();
		assertThat(client.getQpidDelta().getDestinationsFromExchangeName(deliveryExchangeName)).hasSize(1);
	}

	@Test
	public void createMultipleTargetsAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex2", "publicationId = 'pub-1'");
		denmCapability.setShards(Collections.singletonList(shard1));
		client.createHeadersExchange("cap-ex2");

		Capability denmCapability2 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(5)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard2 = new CapabilityShard(1, "cap-ex3", "publicationId = 'pub-1'");
		denmCapability2.setShards(Collections.singletonList(shard2));
		client.createHeadersExchange("cap-ex3");

		String deliveryExchangeName = "my-exchange6";
		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match, match2));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(deliveryExchangeName)).isTrue();
		assertThat(client.getQpidDelta().getDestinationsFromExchangeName(deliveryExchangeName)).hasSize(2);
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedDelivery() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String exchangeName = "my-exchange8";

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex4", "publicationId = 'pub-1'");
		denmCapability.setShards(Collections.singletonList(shard));
		client.createHeadersExchange("cap-ex4");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, exchangeName));
		delivery.setId(1);
		serviceProvider.addDeliveries(Set.of(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.getEndpoints()).isEmpty();
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedCapabilityWhenThereIsNoOtherMatches() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex5", "publicationId = 'pub-1'");
		denmCapability.setShards(Collections.singletonList(shard));
		client.createHeadersExchange("cap-ex5");

		String deliveryExchangeName = "my-exchange9";
		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
		delivery.setId(1);

		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getEndpoints().stream().findFirst().get().getTarget())).isTrue();

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		router.tearDownDeliveryQueues(serviceProvider, client.getQpidDelta());

		assertThat(delivery.getEndpoints()).isEmpty();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
	}

	@Test
	public void removeOneEndpointsWhenOneOfTwoMatchesIsRemoved() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability1 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex6", "publicationId = 'pub-1'");
		denmCapability1.setShards(Collections.singletonList(shard1));
		client.createHeadersExchange("cap-ex6");

		Capability denmCapability2 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1233"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard2 = new CapabilityShard(1, "cap-ex7", "publicationId = 'pub-1'");
		denmCapability2.setShards(Collections.singletonList(shard2));
		client.createHeadersExchange("cap-ex7");

		String deliveryExchangeName = "my-exchange10";
		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", LocalDeliveryStatus.CREATED, "No delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
		delivery.setId(1);

		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match1 = new OutgoingMatch(delivery, denmCapability1, serviceProviderName);
		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match1, match2));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getEndpoints().stream().findFirst().get().getTarget())).isTrue();

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match2));
		router.tearDownDeliveryQueues(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getEndpoints().stream().findFirst().get().getTarget())).isTrue();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
	}

	@Test
	public void deliveryMatchingShardedCapabilityGetsMultipleBindings() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		metadata.setShardCount(3);

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				metadata
		);
		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex12", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex12");

		CapabilityShard shard2 = new CapabilityShard(2, "cap-ex13", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex13");

		CapabilityShard shard3 = new CapabilityShard(3, "cap-ex14", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex14");

		denmCapability.setShards(Arrays.asList(shard1, shard2, shard3));

		String deliveryExchangeName = "my-exchange11";
		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", LocalDeliveryStatus.CREATED, "Delivery");
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
		delivery.setStatus(LocalDeliveryStatus.CREATED);

		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.singletonList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(deliveryExchangeName)).isTrue();
		assertThat(client.getQpidDelta().getDestinationsFromExchangeName(deliveryExchangeName)).hasSize(3);
	}

	@Test
	public void tearDownDeliveryQueueShouldNotChangeRequestedDeliveries() {
		LocalDelivery localDelivery = new LocalDelivery(
				1,
				"a = b",
				LocalDeliveryStatus.REQUESTED
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"no-change-for-requested-delivery-sp",
				new Capabilities(),
				Collections.emptySet(),
				Collections.singleton(
						localDelivery
				),
				null
		);
		QpidDelta delta = client.getQpidDelta();
		when(outgoingMatchRepository.findAllByLocalDelivery_Id(1)).thenReturn(new ArrayList<>());
		router.tearDownDeliveryQueues(serviceProvider,delta);
		assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
	}

	@Test
	public void localSubscriptionConnectsToCapabilityExchange() {
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-1", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-1");

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex8", "publicationId = 'pub-1'");
		denmCapability.setShards(Collections.singletonList(shard));
		client.createHeadersExchange("cap-ex8");
		denmCapability.setStatus(CapabilityStatus.CREATED);

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(Collections.singleton(denmCapability)));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncLocalsubscriptionsToAllCapabilities(mySP, client.getQpidDelta(), Collections.singleton(otherSP));
		verify(serviceProviderRepository, times(2)).save(any());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);
	}

	@Test
	public void localSubscriptionConnectsToCapabilityWithMultipleShards() {
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-2", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-2");

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		metadata.setShardCount(3);

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				metadata
		);
		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex9", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex9");

		CapabilityShard shard2 = new CapabilityShard(2, "cap-ex10", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex10");

		CapabilityShard shard3 = new CapabilityShard(3, "cap-ex11", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex11");

		denmCapability.setShards(Arrays.asList(shard1, shard2, shard3));
		denmCapability.setStatus(CapabilityStatus.CREATED);

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(Collections.singleton(denmCapability)));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncLocalsubscriptionsToAllCapabilities(mySP, client.getQpidDelta(), Collections.singleton(otherSP));

		verify(serviceProviderRepository, times(2)).save(any());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(3);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(3);
	}

	@Test
	public void connectionGetsRemovedWhenCapabilityIsRemoved(){
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-3", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-3");

		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex15", MessageValidatingSelectorCreator.makeSelector(denmCapability, 1));
		client.createHeadersExchange("cap-ex15");

		denmCapability.setShards(Collections.singletonList(shard));
		denmCapability.setStatus(CapabilityStatus.CREATED);

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(Collections.singleton(denmCapability)));

		when(serviceProviderRepository.save(mySP)).thenReturn(mySP);
		when(serviceProviderRepository.save(otherSP)).thenReturn(otherSP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());
		String queueName = subscription.getLocalEndpoints().stream().findFirst().get().getSource();
		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);
		String queue = subscription.getLocalEndpoints().stream().findFirst().get().getSource();
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());
		assertThat(client.getQueuePublishingLinks(queue)).hasSize(0);
		assertThat(subscription.getLocalEndpoints()).hasSize(0);
		assertThat(subscription.getConnections()).hasSize(0);
		assertThat(subscription.getStatus()).isEqualTo(LocalSubscriptionStatus.NO_OVERLAP);
	}

	@Test
	public void localSubscriptionKeepsConnectionToOneCapabilityAndTearsDownAnother() {
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-4", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-4");

		Capability denmCapability1 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex16", "publicationId = 'pub-1'");
		client.createHeadersExchange("cap-ex16");

		denmCapability1.setShards(Collections.singletonList(shard1));
		denmCapability1.setStatus(CapabilityStatus.CREATED);

		Capability denmCapability2 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-2",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard2 = new CapabilityShard(1, "cap-ex17", "publicationId = 'pub-2'");
		client.createHeadersExchange("cap-ex17");

		denmCapability2.setShards(Collections.singletonList(shard2));
		denmCapability2.setStatus(CapabilityStatus.CREATED);

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(denmCapability1, denmCapability2))));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(2);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(2);

		denmCapability1.setStatus(CapabilityStatus.TEAR_DOWN);

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);
	}

	@Test
	public void routerPicksUpRequestedLocalSubscription() {
		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"originatingCountry = 'NO' and messageType = 'DENM'",
				"a.bouvetinterchange.eu"

		);
		Capability capability = new Capability(
				new DenmApplication("NO12345", "NO12345:1", "NO", "DENM:1", List.of("12003"), List.of(6)),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability.setStatus(CapabilityStatus.REQUESTED);
		capability.getShards().add(new CapabilityShard(1, "serviceProvider", MessageValidatingSelectorCreator.makeSelector(capability, 1)));
		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				new Capabilities(Set.of(capability)),
				Collections.singleton(
						localSubscription
				),
				Collections.emptySet(),
				LocalDateTime.now()
		);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions().stream().findFirst().get().getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);
	}

	@Test
	public void testIllegalLocalSubscriptionGetsRemovedFromServiceProvider() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void testLocalSubscriptionWithErrorGetsRemovedFromServiceProvider(){
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ERROR,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void tearDownLocalSubscriptionWithEmptyMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(new ArrayList<>());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void teardownLocalSubscriptionWithRemainingMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		Match match = new Match(
				subscription,
				new Subscription("",SubscriptionStatus.TEAR_DOWN),
				"sp-1"
		);

		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
	}

	@Test
	public void redirectSubscriptionStatusTearDownEmptyMatchList() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);

		when(matchRepository.findAllByLocalSubscriptionId(1)).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue";
		String exchangeName = "sub-exchange";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionsCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-1";
		String exchangeName = "sub-exchange-1";
		String exchangeName2 = "sub-exchange-2";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);
		client.createHeadersExchange(exchangeName2);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Subscription subscription2 = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint2 = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName2));
		subscription2.setEndpoints(Collections.singleton(endpoint2));

		Match match = new Match(localSubscription, subscription, "my-service-provider");
		Match match2 = new Match(localSubscription, subscription2, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match, match2));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(2);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName2));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedBindKeyAlreadyExists() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-4";
		String exchangeName = "sub-exchange-4";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		//Mocking that binding already exists and isn't created again
		client.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionTearDown() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-3";
		String exchangeName = "sub-exchange-3";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(0);
		assertThat(client.getQueuePublishingLinks(queueName)).noneMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedButNoMatchYet() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-5";
		String exchangeName = "sub-exchange-5";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void redirectSubscriptionStatusIllegal() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"sp1"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void createBindingsWithMatchesWhereSubscriptionExchangeIsNotAlreadyCreated() {
		String name = "service-provider-no-subs-exchange-setup";
		String source = "no-subs-exchange-setup-local-sub";
		client.createQueue(source);
		LocalSubscription localSubscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"a = b",
				qpidContainer.getvHostName(),
				Collections.emptySet(),
				Collections.singleton(
						new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				name,
				Collections.singleton(
						localSubscription
				)
		);
		Subscription subscription = new Subscription(
			SubscriptionStatus.REQUESTED,
				"a = b",
				"",
				"a=b"
		);
		String exchangeName = "this-is-my-non-existing-local-exchange";
		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(
				localSubscription,
				subscription
		);
		when(serviceProviderRepository.findAll()).thenReturn(Arrays.asList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(localSubscription.getId())).thenReturn(Arrays.asList(match));
		router.createBindingsWithMatches();

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(client.getQueuePublishingLinks(source)).doesNotContain(new Binding(source, name, new Filter("a = b")));
	}

	@Test
	public void createBindingsWithMatchesWhereLocalSubscriptionQueueIsNotAlreadyCreated() {
		String name = "service-provider-no-local-subs-queue-setup";
		String source = "no-local-subs-queue-setup-local-sub";
		String exchangeName = "this-is-my-existing-local-exchange";
		client.createHeadersExchange(exchangeName);
		LocalSubscription localSubscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"a = b",
				qpidContainer.getvHostName(),
				Collections.emptySet(),
				Collections.singleton(
						new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				name,
				Collections.singleton(
						localSubscription
				)
		);
		Subscription subscription = new Subscription(
				SubscriptionStatus.REQUESTED,
				"a = b",
				"",
				"a=b"
		);
		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(
				localSubscription,
				subscription
		);
		when(serviceProviderRepository.findAll()).thenReturn(Arrays.asList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(localSubscription.getId())).thenReturn(Arrays.asList(match));
		router.createBindingsWithMatches();

		assertThat(client.queueExists(source)).isFalse();
	}

	@Test
	public void localSubscriptionWillBindToMultipleEndpointsFromNeighbour() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-6";
		String exchangeName = "sub-exchange-6";
		String exchangeName2 = "sub-exchange-7";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);
		client.createHeadersExchange(exchangeName2);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint1 = new Endpoint("source1", "host", 5671, new SubscriptionShard(exchangeName));
		Endpoint endpoint2 = new Endpoint("source2", "host", 5671, new SubscriptionShard(exchangeName2));
		subscription.setEndpoints(new HashSet<>(Arrays.asList(endpoint1, endpoint2)));

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(2);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName2));
	}
}
