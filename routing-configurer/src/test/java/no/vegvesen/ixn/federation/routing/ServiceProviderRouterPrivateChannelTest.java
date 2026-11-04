package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.service.routing.localdelivery.LocalDeliveryService;
import no.vegvesen.ixn.federation.service.routing.localsubscription.LocalSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceProviderRouterPrivateChannelTest {

	private static final String SERVICE_PROVIDER = "service-provider";

	@Mock
	private ServiceProviderRepository serviceProviderRepository;

	@Mock
	private PrivateChannelRepository privateChannelRepository;

	@Mock
	private QpidClient qpidClient;

	@Mock
	private LocalSubscriptionService localSubscriptionService;

	@Mock
	private InterchangeNodeProperties nodeProperties;

	@Mock
	private LocalDeliveryService localDeliveryService;

	@Mock
	private OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

	private ServiceProviderRouter router;

	@BeforeEach
	void setUp() {
		router = new ServiceProviderRouter(
				serviceProviderRepository,
				privateChannelRepository,
				qpidClient,
				localSubscriptionService,
				nodeProperties,
				localDeliveryService,
				outgoingMatchDiscoveryService
		);

		when(privateChannelRepository.findAllByPeerNameAndStatus(anyString(), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(anyString(), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(0L);
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(eq(PrivateChannelStatus.CREATED), anyString()))
				.thenReturn(Collections.emptyList());
		when(privateChannelRepository.save(any(PrivateChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(qpidClient.getQpidAcl()).thenAnswer(invocation -> freshAcl());
		when(qpidClient.addPrivateChannelMemberToGroup(anyString()))
				.thenAnswer(invocation -> new PrivateChannelMember(invocation.getArgument(0)));
	}

	private static VirtualHostAccessController freshAcl() {
		List<AclRule> rules = new ArrayList<>();
		rules.add(new AclRule("ALL", "ALL", "DENY-LOG", "ALL", Map.of()));
		return new VirtualHostAccessController("acl-id", rules);
	}

	private static PrivateChannel channel(PrivateChannelStatus status, String queueName, Set<Peer> peers) {
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, queueName);
		return new PrivateChannel(peers, status, endpoint, SERVICE_PROVIDER);
	}

	private static ServiceProvider serviceProvider() {
		return new ServiceProvider(SERVICE_PROVIDER);
	}

	private void stubChannels(PrivateChannel... channels) {
		when(privateChannelRepository.findAllByServiceProviderName(SERVICE_PROVIDER))
				.thenReturn(new ArrayList<>(Arrays.asList(channels)));
	}

	@Test
	void requestedChannelWithNoPeersCreatesQueueAndOnlyGrantsServiceProviderAccess() {
		PrivateChannel channel = channel(PrivateChannelStatus.REQUESTED, "queue-1", Collections.emptySet());
		stubChannels(channel);

		when(qpidClient.createNonDestructiveQueue("queue-1")).thenReturn(new Queue("queue-1"));
		when(qpidClient.addPrivateChannelMemberToGroup(SERVICE_PROVIDER)).thenReturn(new PrivateChannelMember(SERVICE_PROVIDER));

		QpidDelta delta = new QpidDelta(List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(channel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		verify(qpidClient).createNonDestructiveQueue("queue-1");
		verify(qpidClient, times(1)).addPrivateChannelMemberToGroup(anyString());
		verify(qpidClient).addPrivateChannelMemberToGroup(SERVICE_PROVIDER);
		verify(qpidClient).postQpidAcl(any());
	}

	@Test
	void requestedChannelReusesQueueAlreadyPresentInDeltaInsteadOfRecreatingIt() {
		PrivateChannel channel = channel(PrivateChannelStatus.REQUESTED, "queue-existing", Collections.emptySet());
		stubChannels(channel);
		when(qpidClient.addPrivateChannelMemberToGroup(SERVICE_PROVIDER)).thenReturn(new PrivateChannelMember(SERVICE_PROVIDER));

		Queue existingQueue = new Queue("queue-existing");
		QpidDelta delta = new QpidDelta(List.of(), List.of(existingQueue));

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).createNonDestructiveQueue(anyString());
		assertThat(channel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
	}

	@Test
	void requestedChannelDoesNotReAddServiceProviderGroupMembershipWhenAlreadyInDelta() {
		PrivateChannel channel = channel(PrivateChannelStatus.REQUESTED, "queue-2", Collections.emptySet());
		stubChannels(channel);
		when(qpidClient.createNonDestructiveQueue("queue-2")).thenReturn(new Queue("queue-2"));

		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(new PrivateChannelMember(SERVICE_PROVIDER)), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).addPrivateChannelMemberToGroup(SERVICE_PROVIDER);
	}

	@Test
	void requestedChannelWithPeerAlreadyInGroupDoesNotReAddPeerMembershipButStillMarksPeerCreated() {
		Peer peer = new Peer("peer-1");
		PrivateChannel channel = channel(PrivateChannelStatus.REQUESTED, "queue-3", Set.of(peer));
		stubChannels(channel);
		when(qpidClient.createNonDestructiveQueue("queue-3")).thenReturn(new Queue("queue-3"));
		when(qpidClient.addPrivateChannelMemberToGroup(SERVICE_PROVIDER)).thenReturn(new PrivateChannelMember(SERVICE_PROVIDER));

		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(new PrivateChannelMember("peer-1")), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).addPrivateChannelMemberToGroup("peer-1");
		assertThat(peer.getStatus()).isEqualTo(PeerStatus.CREATED);
	}

	@Test
	void createdChannelAddsNewlyRequestedPeerAndGrantsAccess() {
		Peer newPeer = new Peer("new-peer");
		PrivateChannel channel = channel(PrivateChannelStatus.CREATED, "queue-4", Set.of(newPeer));
		stubChannels(channel);
		when(qpidClient.addPrivateChannelMemberToGroup("new-peer")).thenReturn(new PrivateChannelMember("new-peer"));

		QpidDelta delta = new QpidDelta(List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(newPeer.getStatus()).isEqualTo(PeerStatus.CREATED);
		verify(qpidClient).addPrivateChannelMemberToGroup("new-peer");
		verify(privateChannelRepository, atLeastOnce()).save(channel);
	}

	@Test
	void createdChannelRemovesTearDownPeerAndRevokesMembershipWhenNotReferencedElsewhere() {
		Peer peer = new Peer("leaving-peer");
		peer.setStatus(PeerStatus.TEAR_DOWN);
		PrivateChannel channel = channel(PrivateChannelStatus.CREATED, "queue-5", Set.of(peer));
		stubChannels(channel);

		PrivateChannelMember peerMember = new PrivateChannelMember("leaving-peer");
		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(peerMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(channel.getPeers()).isEmpty();
		verify(qpidClient).removePrivateChannelMemberFromGroup(peerMember);
		assertThat(delta.findByPrivateChannelUserName("leaving-peer")).isNull();
	}

	@Test
	void createdChannelKeepsPeerGroupMembershipWhenPeerStillUsedAsPeerInAnotherCreatedChannel() {
		Peer peer = new Peer("shared-peer");
		peer.setStatus(PeerStatus.TEAR_DOWN);
		PrivateChannel channel = channel(PrivateChannelStatus.CREATED, "queue-6", Set.of(peer));
		stubChannels(channel);

		Peer otherPeer = new Peer("shared-peer");
		otherPeer.setStatus(PeerStatus.CREATED);
		PrivateChannel otherChannel = channel(PrivateChannelStatus.CREATED, "queue-6-other", Set.of(otherPeer));
		when(privateChannelRepository.findAllByPeerNameAndStatus(eq("shared-peer"), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(List.of(channel, otherChannel));

		PrivateChannelMember peerMember = new PrivateChannelMember("shared-peer");
		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(peerMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(any());
		assertThat(channel.getPeers()).isEmpty();
	}

	@Test
	void createdChannelKeepsPeerGroupMembershipWhenPeerIsServiceProviderOfAnotherCreatedChannel() {
		Peer peer = new Peer("peer-as-sp");
		peer.setStatus(PeerStatus.TEAR_DOWN);
		PrivateChannel channel = channel(PrivateChannelStatus.CREATED, "queue-7", Set.of(peer));
		stubChannels(channel);

		when(privateChannelRepository.countByServiceProviderNameAndStatus(eq("peer-as-sp"), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(1L);

		PrivateChannelMember peerMember = new PrivateChannelMember("peer-as-sp");
		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(peerMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(any());
	}

	@Test
	void tearDownChannelWithNoPeersRemovesQueueAndServiceProviderMembershipAndDeletesEntity() {
		PrivateChannel channel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-8", Collections.emptySet());
		stubChannels(channel);

		Queue queue = new Queue("queue-8");
		PrivateChannelMember spMember = new PrivateChannelMember(SERVICE_PROVIDER);
		QpidDelta delta = new QpidDelta(List.of(), List.of(queue), List.of(spMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient).removeQueue(queue);
		verify(qpidClient).removePrivateChannelMemberFromGroup(spMember);
		assertThat(delta.findByQueueName("queue-8")).isNull();
		verify(privateChannelRepository).delete(channel);
	}

	@Test
	void tearDownChannelDoesNotThrowWhenQueueIsAlreadyMissingFromDelta() {
		PrivateChannel channel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-9", Collections.emptySet());
		stubChannels(channel);

		QpidDelta delta = new QpidDelta(List.of(), List.of());

		assertThatNoException().isThrownBy(() -> router.syncPrivateChannels(serviceProvider(), delta));

		verify(qpidClient, never()).removeQueue(any());
	}

	@Test
	void tearDownChannelDoesNotReAddServiceProviderMembershipWhenItIsMissingFromDeltaAtStartOfRun() {
		PrivateChannel channel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-10", Collections.emptySet());
		stubChannels(channel);

		Queue queue = new Queue("queue-10");
		QpidDelta delta = new QpidDelta(List.of(), List.of(queue));

		assertThatNoException().isThrownBy(() -> router.syncPrivateChannels(serviceProvider(), delta));

		verify(qpidClient, never()).addPrivateChannelMemberToGroup(anyString());
		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(any());
		verify(qpidClient).removeQueue(queue);
	}

	@Test
	void tearDownChannelKeepsServiceProviderMembershipWhenServiceProviderIsPeerInAnotherCreatedChannel() {
		PrivateChannel channel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-11", Collections.emptySet());
		stubChannels(channel);

		Peer serviceProviderPeer = new Peer(SERVICE_PROVIDER);
		serviceProviderPeer.setStatus(PeerStatus.CREATED);
		PrivateChannel otherChannel = channel(PrivateChannelStatus.CREATED, "queue-12", Set.of(serviceProviderPeer));
		when(privateChannelRepository.findAllByPeerNameAndStatus(eq(SERVICE_PROVIDER), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(List.of(otherChannel));

		Queue queue = new Queue("queue-11");
		PrivateChannelMember spMember = new PrivateChannelMember(SERVICE_PROVIDER);
		QpidDelta delta = new QpidDelta(List.of(), List.of(queue), List.of(spMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(spMember);
	}

	@Test
	void noPrivateChannelsDoesNotTouchQpid() {
		stubChannels();

		router.syncPrivateChannels(serviceProvider(), new QpidDelta(List.of(), List.of()));

		verifyNoInteractions(qpidClient);
	}

	@Test
	void syncingAlreadyCreatedChannelTwiceDoesNotRepeatQpidSideEffects() {
		PrivateChannel channel = channel(PrivateChannelStatus.CREATED, "queue-idempotent", Collections.emptySet());
		stubChannels(channel);

		PrivateChannelMember spMember = new PrivateChannelMember(SERVICE_PROVIDER);
		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(spMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);
		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient, never()).createNonDestructiveQueue(anyString());
		verify(qpidClient, never()).getQpidAcl();
		verify(qpidClient, never()).postQpidAcl(any());
		verify(privateChannelRepository, never()).save(any());
	}

	@Test
	void removingSamePeerFromAllCreatedChannelsRemovesGroupMembershipOnlyOnce() {
		Peer firstPeer = new Peer("shared-leaving-peer");
		firstPeer.setStatus(PeerStatus.TEAR_DOWN);
		Peer secondPeer = new Peer("shared-leaving-peer");
		secondPeer.setStatus(PeerStatus.TEAR_DOWN);
		PrivateChannel firstChannel = channel(PrivateChannelStatus.CREATED, "queue-13", Set.of(firstPeer));
		PrivateChannel secondChannel = channel(PrivateChannelStatus.CREATED, "queue-14", Set.of(secondPeer));
		stubChannels(firstChannel, secondChannel);

		when(privateChannelRepository.findAllByPeerNameAndStatus(eq("shared-leaving-peer"), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(List.of(firstChannel, secondChannel));

		PrivateChannelMember peerMember = new PrivateChannelMember("shared-leaving-peer");
		QpidDelta delta = new QpidDelta(List.of(), List.of(), List.of(peerMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(firstChannel.getPeers()).isEmpty();
		assertThat(secondChannel.getPeers()).isEmpty();
		verify(qpidClient, times(1)).removePrivateChannelMemberFromGroup(peerMember);
		assertThat(delta.findByPrivateChannelUserName("shared-leaving-peer")).isNull();
	}

	@Test
	void tearDownAndRequestForSameServiceProviderKeepsMembershipAndCreatesNewChannel() {
		PrivateChannel tearDownChannel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-old", Collections.emptySet());
		PrivateChannel requestedChannel = channel(PrivateChannelStatus.REQUESTED, "queue-new", Collections.emptySet());
		stubChannels(tearDownChannel, requestedChannel);

		when(qpidClient.createNonDestructiveQueue("queue-new")).thenReturn(new Queue("queue-new"));

		Queue oldQueue = new Queue("queue-old");
		QpidDelta delta = new QpidDelta(List.of(), List.of(oldQueue));

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(requestedChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		verify(qpidClient).addPrivateChannelMemberToGroup(SERVICE_PROVIDER);
		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(any());
		verify(qpidClient).createNonDestructiveQueue("queue-new");
		verify(qpidClient).removeQueue(argThat(queue -> queue.getName().equals("queue-old")));
		assertThat(delta.findByPrivateChannelUserName(SERVICE_PROVIDER)).isNotNull();
	}

	@Test
	void tearDownChannelRemovesOnlyUnusedPeerMembership() {
		Peer unusedPeer = new Peer("unused-leaving-peer");
		unusedPeer.setStatus(PeerStatus.TEAR_DOWN);
		Peer usedPeer = new Peer("used-leaving-peer");
		usedPeer.setStatus(PeerStatus.TEAR_DOWN);
		PrivateChannel channel = channel(PrivateChannelStatus.TEAR_DOWN, "queue-15", Set.of(unusedPeer, usedPeer));
		stubChannels(channel);

		Peer remainingPeer = new Peer("used-leaving-peer");
		remainingPeer.setStatus(PeerStatus.CREATED);
		PrivateChannel otherChannel = channel(PrivateChannelStatus.CREATED, "queue-16", Set.of(remainingPeer));
		when(privateChannelRepository.findAllByPeerNameAndStatus(eq("used-leaving-peer"), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(List.of(otherChannel));

		PrivateChannelMember unusedMember = new PrivateChannelMember("unused-leaving-peer");
		PrivateChannelMember usedMember = new PrivateChannelMember("used-leaving-peer");
		QpidDelta delta = new QpidDelta(List.of(), List.of(new Queue("queue-15")), List.of(unusedMember, usedMember), List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		verify(qpidClient).removePrivateChannelMemberFromGroup(unusedMember);
		verify(qpidClient, never()).removePrivateChannelMemberFromGroup(usedMember);
	}
}
