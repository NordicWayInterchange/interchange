package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.onboard.SelfService;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {NeighbourService.class, CapabilitiesService.class, InterchangeNodeProperties.class, GracefulBackoffProperties.class})
public class NeighbourServiceDiscoveryTest {

	@MockBean
	private NeighbourRepository neighbourRepository;
	@MockBean
	private ListenerEndpointRepository listenerEndpointRepository;
	@MockBean
	private DNSFacade dnsFacade;
	@MockBean
	private NeighbourFacade neighbourFacade;
	@MockBean
	private NeighbourDiscovererProperties discovererProperties;
	@MockBean
	private SelfService selfService;

	@Autowired
	InterchangeNodeProperties interchangeNodeProperties;

	@Autowired
	private NeighbourService neighbourService;

	@Autowired
	private CapabilitiesService capabilitiesService;

	private Self self;

	private Capability getDatexCapability(String originatingCountry) {
		return new DatexCapability(null, originatingCountry, null, null, null);
	}

	@BeforeEach
	public void before(){
		self = createSelf();
	}

	private Neighbour createNeighbour() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("ericsson.itsinterchange.eu");
		neighbour.setControlChannelPort("8080");
		return neighbour;
	}

	private Self createSelf() {
		// Self setup
		self = new Self(interchangeNodeProperties.getName());
		Set<Capability> selfCapabilities = Collections.singleton(getDatexCapability("NO"));
		self.setLocalCapabilities(selfCapabilities);
		Set<LocalSubscription> selfSubscriptions = new HashSet<>();
		selfSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and originatingCountry = 'NO'"));
		self.setLocalSubscriptions(selfSubscriptions);
		self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());
		return self;
	}

	@Test
	public void testNewNeighbourIsAddedToDatabase(){
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(createNeighbour()));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(null);
		when(neighbourRepository.save(any(Neighbour.class))).thenReturn(mock(Neighbour.class));

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void testKnownNeighbourIsNotAddedToDatabase(){
		Neighbour ericsson = createNeighbour();
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	@Test
	public void testNeighbourWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		Neighbour discoveringNode = new Neighbour();
		discoveringNode.setName(self.getName());
		assertThat(self.getName()).isNotNull();
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository,times(1)).findByName(interchangeNodeProperties.getName());
		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	//TODO Make a test for having a Neighbour that is in the database, but not in DNS (in other words, removed).

	/**
	 * Capabilities exchange and subscription request
	 */

	@Test
	public void successfulCapabilitiesPostCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Capabilities capabilities = new Capabilities();
		assertThat(capabilities.getCapabilities()).isEmpty();

		doReturn(capabilities).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));


		doReturn(self).when(selfService).fetchSelf();

		capabilitiesService.capabilityExchange(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulSubscriptionRequestCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourFacade).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		Capabilities no = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		no.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		ericsson.setCapabilities(no);

		doReturn(self).when(selfService).fetchSelf();

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	private SubscriptionRequest createFirstSubscriptionRequestResponse() {
		Subscription firstSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		return new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
	}

	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.getControlConnection().setBackoffStart(futureTime);
		ericsson.getControlConnection().setConnectionStatus(ConnectionStatus.FAILED);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		capabilitiesService.capabilityExchange(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourFacade, times(0)).postCapabilitiesToCapabilities(any(Neighbour.class), any() );
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.getControlConnection().setBackoffStart(futureTime);
		ericsson.getControlConnection().setConnectionStatus(ConnectionStatus.FAILED);
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("FI"))));
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(invocationOnMock -> {
			Neighbour answer = invocationOnMock.getArgument(0);
			answer.setNeighbour_id(1);
			return answer;
		});

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourFacade, times(0)).postSubscriptionRequest(any(), anySet(), anyString());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription to poll
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED, false, "");
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		ericsson.getControlConnection().setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neighbourService.pollSubscriptions(neighbourFacade);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		verify(neighbourFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionWithStatusCreatedDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription with status created to poll
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		ericsson.getControlConnection().setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neighbourService.pollSubscriptionsWithStatusCreated(neighbourFacade);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		verify(neighbourFacade, times(0)).pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();

		Capability firstDataType = getDatexCapability("NO");
		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));

		doReturn(ericssonCapabilities).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any());

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		capabilitiesService.capabilityExchange(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourFacade, times(1)).postCapabilitiesToCapabilities(any(Neighbour.class), any());
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourFacade).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString() );
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(self).when(selfService).fetchSelf();
		Capabilities noCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		noCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		ericsson.setCapabilities(noCapabilities);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), self, neighbourFacade);

		verify(neighbourFacade, times(1)).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED, false, "");
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		doReturn(ericssonSubscription).when(neighbourFacade).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(neighbourFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionWithStatusCreatedHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		ericssonSubscription.setLastUpdatedTimestamp(1);
		Broker broker = new Broker("queue-1", "broker-1");
		ericssonSubscription.setBrokers(Sets.newLinkedHashSet(broker));
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		//doReturn(ericssonSubscription).when(neighbourFacade).pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class));

		Subscription ericssonSubscriptionUpdated = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		ericssonSubscriptionUpdated.setLastUpdatedTimestamp(2);
		ericssonSubscriptionUpdated.setBrokers(Sets.newLinkedHashSet(broker));
		doReturn(ericssonSubscriptionUpdated).when(neighbourFacade).pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(neighbourFacade, times(1)).pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		Neighbour ericsson = createNeighbour();
		ericsson.getControlConnection().setBackoffAttempts(0);
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);

		ericsson.getControlConnection().setBackoffStart(pastTime);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any());
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		capabilitiesService.capabilityExchange(Collections.singletonList(ericsson), self, neighbourFacade);

		assertThat(ericsson.getControlConnection().getBackoffAttempts()).isEqualTo(1);
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeWithTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		Neighbour ericsson = createNeighbour();
		ericsson.getControlConnection().setBackoffAttempts(4);

		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.FAILED, Collections.singleton(getDatexCapability("NO")));
		ericsson.setCapabilities(ericssonCapabilities);

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any());

		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		capabilitiesService.capabilityExchange(Collections.singletonList(ericsson), self, neighbourFacade);

		assertThat(ericsson.getControlConnection().getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);
		verify(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any());

	}

	/*
		Subscription polling tests
	 */

	@Test
	public void successfulPollOfSubscriptionCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, false, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulPollOfSubscriptionWithStatusCreatedCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		Broker broker = new Broker("queue-1", "broker-1");
		subscription.setBrokers(Sets.newLinkedHashSet(broker));
		subscription.setLastUpdatedTimestamp(1);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		polledSubscription.setBrokers(Sets.newLinkedHashSet(broker));
		polledSubscription.setLastUpdatedTimestamp(2);
		when(neighbourFacade.pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulPollOfSubscriptionWithBrokersCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		Broker broker1 = new Broker("queue-1", "broker-1");
		Broker broker2 = new Broker("queue-2", "broker-2");
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		polledSubscription.setBrokers(Sets.newLinkedHashSet(broker1, broker2));
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		when(listenerEndpointRepository.save(any(ListenerEndpoint.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void allSubscriptionsHaveFinalStatusFlipsFedInStatusToEstablished(){

		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour.setName("neighbour-name");
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getSubscriptions()).contains(createdSubscription);
	}

	@Test
	public void allSubscriptionsRejectedFlipsFedInStatusToRejected(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour.setName("neighbour-name");
		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REJECTED, false, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getAcceptedSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptionStatusAcceptedKeepsFedInStatusRequested(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, false, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.REQUESTED);
	}

	//TODO: test why local subscriptions for self must be different from subscription request for neighbours
	// Only capabilities of neighbour and subscription of self is relevant for matching
	// To introduce error make sure part 1 and part 2 below has same selector filter
	@Test
	public void subscriptionRequestProcessesAllNeighboursDespiteNetworkError() {
		HashSet<Capability> capabilitiesNo = new HashSet<>();
		capabilitiesNo.add(getDatexCapability("NO"));
		Capabilities capabilitiesNO = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, capabilitiesNo);
		Neighbour neighbourA = new Neighbour("a", capabilitiesNO, getEmptySR(), getEmptySR());
		Neighbour neighbourB = new Neighbour("b", capabilitiesNO, getEmptySR(), getEmptySR());
		Neighbour neighbourC = new Neighbour("c", capabilitiesNO, getEmptySR(), getEmptySR());
		capabilitiesNO.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));

		List<Neighbour> neighbours = new LinkedList<>();
		neighbours.add(neighbourA);
		neighbours.add(neighbourB);
		neighbours.add(neighbourC);


		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		when(neighbourFacade.postSubscriptionRequest(eq(neighbourA),anySet(), anyString())).thenReturn(getReturnedSubscriptionRequest());
		when(neighbourFacade.postSubscriptionRequest(eq(neighbourB),anySet(), anyString())).thenThrow(new SubscriptionRequestException("time-out"));
		when(neighbourFacade.postSubscriptionRequest(eq(neighbourC),anySet(), anyString())).thenReturn(getReturnedSubscriptionRequest());

		// Self setup
		Self discoveringNode = new Self("d");
		Set<Capability> selfCapabilities = new HashSet<>();
		selfCapabilities.add(getDatexCapability("NO"));
		discoveringNode.setLocalCapabilities(selfCapabilities);

		Set<LocalSubscription> selfSubscriptions = new HashSet<>();
		selfSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'NO'"));
		discoveringNode.setLastUpdatedLocalSubscriptions(LocalDateTime.now());

		discoveringNode.setLocalSubscriptions(selfSubscriptions);

		neighbourService.evaluateAndPostSubscriptionRequest(neighbours, discoveringNode, neighbourFacade);

		verify(neighbourFacade, times(3)).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
	}

	private SubscriptionRequest getEmptySR() {
		Set<Subscription> subscription = new HashSet<>();
		return new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, subscription);
	}

	private SubscriptionRequest getReturnedSubscriptionRequest() {
		HashSet<Subscription> returnedSubscriptionsFromNeighbour = new HashSet<>();
		returnedSubscriptionsFromNeighbour.add(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, false, ""));

		return new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, returnedSubscriptionsFromNeighbour);
	}

	@Test
	public void calculatedSubscriptionRequestSameAsNeighbourSubscriptionsAllowsNextNeighbourToBeSaved() {
		Self self = new Self("self");
		Set<LocalSubscription> selfLocalSubscriptions = new HashSet<>();
		selfLocalSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'NO'"));
		self.setLocalSubscriptions(selfLocalSubscriptions);
		self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet());
		Capability neighbourCapability = getDatexCapability("NO");
		Set<Capability> capabilitySet = new HashSet<>();
		capabilitySet.add(neighbourCapability);
		Capabilities neighbourCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,capabilitySet);
		neighbourCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilities,subscriptionRequest,new SubscriptionRequest());
		Set<Subscription> neighbourFedInSubscription = new HashSet<>();
		neighbourFedInSubscription.add(new Subscription("originatingCountry = 'NO'",SubscriptionStatus.ACCEPTED, false, ""));
		neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(null,neighbourFedInSubscription));

		assertThat(neighbour.hasEstablishedSubscriptions()).isTrue();
		Set<Subscription> subscriptions = neighbourService.calculateCustomSubscriptionForNeighbour(neighbour, selfLocalSubscriptions);
		assertThat(subscriptions.isEmpty()).isFalse();
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).isEqualTo(subscriptions);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		Capabilities otherNeighbourCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		otherNeighbourCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				otherNeighbourCapabilities,
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());

		doReturn(self).when(selfService).fetchSelf();
		when(neighbourFacade.postSubscriptionRequest(any(), any(), any())).thenReturn(mock(SubscriptionRequest.class));

		neighbourService.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour,otherNeighbour), self, neighbourFacade);

		verify(neighbourRepository).save(otherNeighbour);
    }

	@Test
	void unreachableNeighboursWillReceiveCapabilityExchangeWhenRetryingUnreachableNeighbours() {
		Neighbour n1 = new Neighbour();
		n1.setName("neighbour-one");
		n1.getControlConnection().setConnectionStatus(ConnectionStatus.UNREACHABLE);
		n1.getControlConnection().setBackoffStart(LocalDateTime.now().minusDays(2));

		Neighbour n2 = new Neighbour();
		n2.setName("neighbour-two");
		n2.getControlConnection().setConnectionStatus(ConnectionStatus.UNREACHABLE);
		n2.getControlConnection().setBackoffStart(LocalDateTime.now().minusDays(2));

		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in
		when(neighbourRepository.findByControlConnection_ConnectionStatus(ConnectionStatus.UNREACHABLE)).thenReturn(Lists.list(n1, n2));
		when(neighbourFacade.postCapabilitiesToCapabilities(any(), any())).thenReturn(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>()));

		capabilitiesService.retryUnreachable(self, neighbourFacade);

		verify(neighbourFacade, times(2)).postCapabilitiesToCapabilities(any(), any());
	}

	@Test
	public void listenerEndpointIsSavedWhenSubscriptionWithCreatedStatusIsPolled(){
		Neighbour spyNeighbour1 = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		Set<Broker> brokers = new HashSet<>();
		Broker broker = new Broker("spy-neighbour1","amqps://spy-neighbour1");
		brokers.add(broker);
		createdSubscription.setBrokers(brokers);


		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository).save(any(ListenerEndpoint.class));
	}

	@Test
	public void listenerEndpointsAreSavedWhenSubscriptionWithCreatedStatusAndBrokersIsPolled(){
		Neighbour spyNeighbour1 = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		Broker broker = new Broker("spy-neighbour1", "amqps://spy-neighbour1");
		createdSubscription.setBrokers(Sets.newLinkedHashSet(broker));


		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository).save(any(ListenerEndpoint.class));
	}

	@Test
	public void listenerEndpointsAreSavedWhenSubscriptionWithCreatedStatusAndMultipleBrokersIsPolled(){
		Neighbour spyNeighbour1 = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		subscription.setNumberOfPolls(0);
		subscription.setLastUpdatedTimestamp(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		Broker broker1 = new Broker("spy-neighbour1", "amqps://spy-neighbour1");
		Broker broker2 = new Broker("spy-neighbour2", "amqps://spy-neighbour2");
		createdSubscription.setBrokers(Sets.newLinkedHashSet(broker1, broker2));
		createdSubscription.setLastUpdatedTimestamp(2);

		when(neighbourFacade.pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		when(listenerEndpointRepository.save(any(ListenerEndpoint.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		neighbourService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void listenerEndpointsAreRemovedWhenBrokersListIsUpdated() {
		Neighbour spyNeighbour1 = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		subscription.setNumberOfPolls(0);
		subscription.setLastUpdatedTimestamp(0);
		Broker broker = new Broker("spy-neighbour", "amqps://spy-neighbour");
		subscription.setBrokers(Sets.newLinkedHashSet(broker));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, false, "");
		createdSubscription.setLastUpdatedTimestamp(2);

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(spyNeighbour1.getName(), "amqps://spy-neighbour", "spy-neighbour", new Connection());

		when(neighbourFacade.pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(listenerEndpointRepository.findByNeighbourNameAndBrokerUrlAndQueue(spyNeighbour1.getName(), broker.getMessageBrokerUrl(), broker.getQueueName())).thenReturn(listenerEndpoint);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(listenerEndpointRepository, times(1)).delete(any(ListenerEndpoint.class));

	}

	@Test
	public void listenerEndpointIsNotSavedWhenSubscriptionWithRequestedStatusIsPolled(){
		Neighbour spyNeighbour1 = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, false, "");
		Set<Broker> brokers = new HashSet<>();
		Broker broker = new Broker("spy-neighbour1","amqps://spy-neighbour1");
		brokers.add(broker);
		createdSubscription.setBrokers(brokers);


		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository, times(0)).save(any(ListenerEndpoint.class));
	}
}
