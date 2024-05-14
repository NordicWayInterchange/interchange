package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.subscription.SubscriptionCalculator;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {NeighbourService.class, NeigbourDiscoveryService.class, InterchangeNodeProperties.class})
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
	private GracefulBackoffProperties backoffProperties;
	@Autowired
	InterchangeNodeProperties interchangeNodeProperties;

	@Autowired
	private NeighbourService neighbourService;

	@Autowired
	private NeigbourDiscoveryService neigbourDiscoveryService;

	private LocalDateTime now = LocalDateTime.now();


	private NeighbourCapability getDatexCapability(String originatingCountry) {
		return new NeighbourCapability(
				new DatexApplication(originatingCountry + "-123", originatingCountry + "-pub", originatingCountry, "1.0", Collections.singleton("0122"),"SituationPublication"),
				new Metadata(RedirectStatus.OPTIONAL));
	}
	private CapabilitySplit getDatexCapabilitySplit(String originatingCountry) {
		return new CapabilitySplit(
				new DatexApplication(originatingCountry + "-123", originatingCountry + "-pub", originatingCountry, "1.0", Collections.singleton("0122"),"SituationPublication"),
				new Metadata(RedirectStatus.OPTIONAL));
	}

	@BeforeEach
	public void before(){
	}

	private Neighbour createNeighbour() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("ericsson.itsinterchange.eu");
		neighbour.setControlChannelPort("8080");
		return neighbour;
	}

	private Set<LocalSubscription> getLocalSubscriptions() {
		Set<LocalSubscription> selfSubscriptions = new HashSet<>();
		selfSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and originatingCountry = 'NO'", interchangeNodeProperties.getName()));
		return selfSubscriptions;
	}

	private Set<CapabilitySplit> getSelfCapabilities() {
		return Collections.singleton(getDatexCapabilitySplit("NO"));
	}

	@Test
	public void testNewNeighbourIsAddedToDatabase(){
		Neighbour neighbour = new Neighbour();
		neighbour.setName("ericsson.itsinterchange.eu");
		neighbour.setControlChannelPort("8080");
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(neighbour));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(null);
		when(neighbourRepository.save(any(Neighbour.class))).thenReturn(mock(Neighbour.class));

		neigbourDiscoveryService.checkForNewNeighbours();

		verify(neighbourRepository, times(1)).save(neighbour);
	}

	@Test
	public void testKnownNeighbourIsNotAddedToDatabase(){
		Neighbour ericsson = createNeighbour();
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(ericsson);

		neigbourDiscoveryService.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	@Test
	public void testNeighbourWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		Neighbour discoveringNode = new Neighbour();
		discoveringNode.setName(interchangeNodeProperties.getName());
		assertThat(interchangeNodeProperties.getName()).isNotNull();
		when(dnsFacade.lookupNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neigbourDiscoveryService.checkForNewNeighbours();

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

		doReturn(capabilities.getCapabilities()).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));


		Optional<LocalDateTime> lastUpdatedLocalCapabilities = Optional.of(now);
		neigbourDiscoveryService.capabilityExchange(Collections.singletonList(ericsson), neighbourFacade,  getSelfCapabilities(), lastUpdatedLocalCapabilities);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulSubscriptionRequestCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourFacade).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		NeighbourCapabilities no = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		no.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		ericsson.setCapabilities(no);


		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.of(now);
		Set<LocalSubscription> localSubscriptions = getLocalSubscriptions();
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	private Set<Subscription> createFirstSubscriptionRequestResponse() {
		Subscription firstSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		return Collections.singleton(firstSubscription);
	}

	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.getControlConnection().setBackoffStart(futureTime);
		ericsson.getControlConnection().setConnectionStatus(ConnectionStatus.FAILED);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		Optional<LocalDateTime> lastUpdatedLocalCapabilities = Optional.of(now);
		neigbourDiscoveryService.capabilityExchange(Collections.singletonList(ericsson), neighbourFacade, getSelfCapabilities(), lastUpdatedLocalCapabilities);

		verify(neighbourFacade, times(0)).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.getControlConnection().setBackoffStart(futureTime);
		ericsson.getControlConnection().setConnectionStatus(ConnectionStatus.FAILED);
		ericsson.setCapabilities(new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("FI"))));
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(invocationOnMock -> {
			Neighbour answer = invocationOnMock.getArgument(0);
			answer.setNeighbour_id(1);
			return answer;
		});

		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.of(now);
		Set<LocalSubscription> localSubscriptions = getLocalSubscriptions();
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);

		verify(neighbourFacade, times(0)).postSubscriptionRequest(any(), anySet(), anyString());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription to poll
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED, "");
		SubscriptionRequest subReq = new SubscriptionRequest(Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		ericsson.getControlConnection().setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		verify(neighbourFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionWithStatusCreatedDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription with status created to poll
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		SubscriptionRequest subReq = new SubscriptionRequest(Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		ericsson.getControlConnection().setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neigbourDiscoveryService.pollSubscriptionsWithStatusCreated(neighbourFacade);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		verify(neighbourFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();

		NeighbourCapability firstDataType = getDatexCapability("NO");
		Set<NeighbourCapability> capabilities = Collections.singleton(firstDataType);

		doReturn(capabilities).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		Optional<LocalDateTime> lastUpdatedLocalCapabilities = Optional.of(now);
		neigbourDiscoveryService.capabilityExchange(Collections.singletonList(ericsson), neighbourFacade, getSelfCapabilities(), lastUpdatedLocalCapabilities);

		verify(neighbourFacade, times(1)).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourFacade).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString() );
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		NeighbourCapabilities noCapabilities = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		noCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		ericsson.setCapabilities(noCapabilities);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.of(now);
		Set<LocalSubscription> localSubscriptions = getLocalSubscriptions();
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson), lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);

		verify(neighbourFacade, times(1)).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any(SubscriptionStatus[].class))).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED, "");
		SubscriptionRequest subReq = new SubscriptionRequest(Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		doReturn(ericssonSubscription).when(neighbourFacade).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		verify(neighbourFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionWithStatusCreatedHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		ericssonSubscription.setLastUpdatedTimestamp(1);
		Endpoint endpoint = new Endpoint("source-1", "host-1", 5671);
		ericssonSubscription.setEndpoints(Sets.newLinkedHashSet(endpoint));
		SubscriptionRequest subReq = new SubscriptionRequest(Collections.singleton(ericssonSubscription));
		ericsson.setOurRequestedSubscriptions(subReq);
		//doReturn(ericssonSubscription).when(neighbourFacade).pollSubscriptionLastUpdatedTime(any(Subscription.class), any(Neighbour.class));

		Subscription ericssonSubscriptionUpdated = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		ericssonSubscriptionUpdated.setLastUpdatedTimestamp(2);
		ericssonSubscriptionUpdated.setEndpoints(Sets.newLinkedHashSet(endpoint));
		doReturn(ericssonSubscriptionUpdated).when(neighbourFacade).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(neighbourFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		Neighbour ericsson = createNeighbour();
		ericsson.getControlConnection().setBackoffAttempts(0);
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);

		ericsson.getControlConnection().setBackoffStart(pastTime);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		Optional<LocalDateTime> lastUpdatedLocalCapabilities = Optional.of(now);
		neigbourDiscoveryService.capabilityExchange(Collections.singletonList(ericsson), neighbourFacade, getSelfCapabilities(), lastUpdatedLocalCapabilities);

		assertThat(ericsson.getControlConnection().getBackoffAttempts()).isEqualTo(1);
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeWithTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		Neighbour ericsson = createNeighbour();
		ericsson.getControlConnection().setBackoffAttempts(4);

		NeighbourCapabilities ericssonCapabilities = new NeighbourCapabilities(CapabilitiesStatus.FAILED, Collections.singleton(getDatexCapability("NO")));
		ericsson.setCapabilities(ericssonCapabilities);

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.getControlConnection().setBackoffStart(pastTime);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());

		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		Optional<LocalDateTime> lastUpdatedLocalCapabilities = Optional.of(now);
		neigbourDiscoveryService.capabilityExchange(Collections.singletonList(ericsson), neighbourFacade, getSelfCapabilities(), lastUpdatedLocalCapabilities);

		assertThat(ericsson.getControlConnection().getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);
		verify(neighbourFacade).postCapabilitiesToCapabilities(any(Neighbour.class), any(), any());

	}

	/*
		Subscription polling tests
	 */

	@Test
	public void successfulPollOfSubscriptionCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any(SubscriptionStatus[].class))).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulPollOfSubscriptionWithStatusCreatedCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		Endpoint endpoint = new Endpoint("source-1", "host-1", 5671);
		subscription.setEndpoints(Sets.newLinkedHashSet(endpoint));
		subscription.setLastUpdatedTimestamp(1);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		polledSubscription.setEndpoints(Sets.newLinkedHashSet(endpoint));
		polledSubscription.setLastUpdatedTimestamp(2);
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptionsWithStatusCreated(neighbourFacade);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	@Disabled
	public void successfulPollOfSubscriptionWithEndpointsCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, interchangeNodeProperties.getName());
		Endpoint endpoint1 = new Endpoint("source-1", "host-1", 5671);
		Endpoint endpoint2 = new Endpoint("source-2", "host-2", 5671);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(Collections.singleton(subscription));
		ericsson.setOurRequestedSubscriptions(ericssonSubscription);
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, interchangeNodeProperties.getName());
		polledSubscription.setEndpoints(Sets.newLinkedHashSet(endpoint1, endpoint2));
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		when(listenerEndpointRepository.save(any(ListenerEndpoint.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void allSubscriptionsHaveFinalStatusFlipsFedInStatusToEstablished(){

		Neighbour spyNeighbour = new Neighbour();

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour.setName("neighbour-name");
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getSubscriptions()).contains(createdSubscription);
	}

	@Test
	public void allSubscriptionsRejectedFlipsFedInStatusToRejected(){
		Neighbour spyNeighbour = new Neighbour();

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour.setName("neighbour-name");
		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REJECTED, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.ACCEPTED)).isEmpty();
	}

	@Test
	public void subscriptionStatusAcceptedKeepsFedInStatusRequested(){
		Neighbour spyNeighbour = new Neighbour();

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(Collections.singleton(subscription));
		spyNeighbour.setOurRequestedSubscriptions(subscriptionRequest);

		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "");
		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		assertThat(spyNeighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
	}

	//TODO: test why local subscriptions be different from subscription request for neighbours
	// Only capabilities of neighbour and local subscriptions  are relevant for matching
	// To introduce error make sure part 1 and part 2 below has same selector filter
	@Test
	public void subscriptionRequestProcessesAllNeighboursDespiteNetworkError() {
		HashSet<NeighbourCapability> capabilitiesNo = new HashSet<>();
		capabilitiesNo.add(getDatexCapability("NO"));
		NeighbourCapabilities capabilitiesNO = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, capabilitiesNo);
		Neighbour neighbourA = new Neighbour("a", capabilitiesNO, getEmptyNeighSR(), getEmptySR());
		Neighbour neighbourB = new Neighbour("b", capabilitiesNO, getEmptyNeighSR(), getEmptySR());
		Neighbour neighbourC = new Neighbour("c", capabilitiesNO, getEmptyNeighSR(), getEmptySR());
		capabilitiesNO.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));

		List<Neighbour> neighbours = new ArrayList<>();
		neighbours.add(neighbourA);
		neighbours.add(neighbourB);
		neighbours.add(neighbourC);


		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		when(neighbourFacade.postSubscriptionRequest(eq(neighbourA),anySet(), anyString())).thenReturn(getReturnedSubscriptionRequest());
		when(neighbourFacade.postSubscriptionRequest(eq(neighbourB),anySet(), anyString())).thenThrow(new SubscriptionRequestException("time-out"));
		when(neighbourFacade.postSubscriptionRequest(eq(neighbourC),anySet(), anyString())).thenReturn(getReturnedSubscriptionRequest());

		Set<CapabilitySplit> selfCapabilities = new HashSet<>();
		selfCapabilities.add(getDatexCapabilitySplit("NO"));

		Set<LocalSubscription> selfSubscriptions = new HashSet<>();
		selfSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'NO'", interchangeNodeProperties.getName()));

		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(neighbours,Optional.of(LocalDateTime.now()), selfSubscriptions, neighbourFacade);

		verify(neighbourFacade, times(3)).postSubscriptionRequest(any(Neighbour.class),anySet(), anyString());
	}

	private SubscriptionRequest getEmptySR() {
		return new SubscriptionRequest(Collections.emptySet());
	}

	private NeighbourSubscriptionRequest getEmptyNeighSR() {
		return new NeighbourSubscriptionRequest(Collections.emptySet());
	}

	private Set<Subscription> getReturnedSubscriptionRequest() {
		HashSet<Subscription> returnedSubscriptionsFromNeighbour = new HashSet<>();
		returnedSubscriptionsFromNeighbour.add(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, ""));

		return returnedSubscriptionsFromNeighbour;
	}

	@Test
	public void calculatedSubscriptionRequestSameAsNeighbourSubscriptionsAllowsNextNeighbourToBeSaved() {
		Set<LocalSubscription> selfLocalSubscriptions = new HashSet<>();
		selfLocalSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", interchangeNodeProperties.getName()));
		LocalDateTime lastUpdatedLocalSubscriptions = LocalDateTime.now();

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(Collections.emptySet());
		NeighbourCapability neighbourCapability = getDatexCapability("NO");
		Set<NeighbourCapability> capabilitySet = new HashSet<>();
		capabilitySet.add(neighbourCapability);
		NeighbourCapabilities neighbourCapabilities = new NeighbourCapabilities(CapabilitiesStatus.KNOWN,capabilitySet);
		neighbourCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilities,subscriptionRequest,new SubscriptionRequest());
		Set<Subscription> neighbourFedInSubscription = new HashSet<>();
		neighbourFedInSubscription.add(new Subscription("originatingCountry = 'NO'",SubscriptionStatus.ACCEPTED, interchangeNodeProperties.getName()));
		neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(neighbourFedInSubscription));

		Set<Subscription> subscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(selfLocalSubscriptions, CapabilitySplit.transformNeighbourCapabilityToSplitCapability(capabilitySet), interchangeNodeProperties.getName());
		assertThat(subscriptions.isEmpty()).isFalse();
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).isEqualTo(subscriptions);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		NeighbourCapabilities otherNeighbourCapabilities = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		otherNeighbourCapabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				otherNeighbourCapabilities,
				new NeighbourSubscriptionRequest(Collections.emptySet()),
				new SubscriptionRequest());

		when(neighbourFacade.postSubscriptionRequest(any(), any(), any())).thenReturn(Collections.emptySet());

		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour,otherNeighbour), Optional.of(lastUpdatedLocalSubscriptions), selfLocalSubscriptions, neighbourFacade);

		verify(neighbourRepository).save(otherNeighbour);
    }

	@Test
	public void listenerEndpointIsNotSavedWhenSubscriptionWithRequestedStatusIsPolled() {
		Neighbour spyNeighbour1 = new Neighbour();

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, interchangeNodeProperties.getName());
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(Collections.singleton(subscription));
		spyNeighbour1.setOurRequestedSubscriptions(subscriptionRequest);
		spyNeighbour1.setName("spy-neighbour1");

		when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour1));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, interchangeNodeProperties.getName());
		Set<Endpoint> endpoints = new HashSet<>();
		Endpoint endpoint = new Endpoint("spy-neighbour1", "spy-neighbour1", 5671);
		endpoints.add(endpoint);
		createdSubscription.setEndpoints(endpoints);


		when(neighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);

		verify(listenerEndpointRepository, times(0)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void testPollOneNeighbour() {
		Subscription subscription = new Subscription(
				1,
				SubscriptionStatus.CREATED,
				"originatingCountry = 'NO'",
				"/mynode/subscriptions/1",
				"kyrre",
				Collections.emptySet()
		);
		Neighbour  neighbour = new Neighbour(
				"test",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								subscription
						)
				),
				new Connection()
		);
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.CREATED)).hasSize(1);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(1); //TODO what???
		when(backoffProperties.getNumberOfAttempts()).thenReturn(1); //TODO this is the one in use!

		when(neighbourFacade.pollSubscriptionStatus(subscription,neighbour)).thenThrow(new SubscriptionPollException("Error"));
		neigbourDiscoveryService.pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour,neighbourFacade);
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.FAILED);
		assertThat(subscription.getNumberOfPolls()).isEqualTo(0); //TODO we start at 0, which mght be a bit confusing. The first fail is not a backoff attempt, by the looks of things.
		neigbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour,neighbourFacade);
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.FAILED);
		assertThat(subscription.getNumberOfPolls()).isEqualTo(1);
		neigbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour,neighbourFacade);
		assertThat(subscription.getNumberOfPolls()).isEqualTo(1); //TODO there was not actually a poll on the last call, so the attempts should not be changed.
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.GIVE_UP);
		//TODO test that this actually works the same way when we do it through the triggered methods.
		verify(neighbourFacade,times(2)).pollSubscriptionStatus(any(),any());
	}

	@Test
	public void testPollOneNeighbourAndGetStatusResubscribeBack() {
		Subscription subscription = new Subscription(
				1,
				SubscriptionStatus.CREATED,
				"originatingCountry = 'NO'",
				"/mynode/subscriptions/1",
				"kyrre",
				Collections.emptySet()
		);
		Neighbour  neighbour = new Neighbour(
				"test",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								subscription
						)
				),
				new Connection()
		);

		Subscription returnSubscription = new Subscription();
		returnSubscription.setSubscriptionStatus(SubscriptionStatus.RESUBSCRIBE);
		returnSubscription.setSelector(subscription.getSelector());
		returnSubscription.setPath(subscription.getPath());
		returnSubscription.setConsumerCommonName(subscription.getConsumerCommonName());

		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.CREATED)).hasSize(1);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(1);
		when(backoffProperties.getNumberOfAttempts()).thenReturn(1);

		when(neighbourFacade.pollSubscriptionStatus(subscription,neighbour)).thenReturn(returnSubscription);
		neigbourDiscoveryService.pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour,neighbourFacade);

		assertThat(subscription.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
		verify(neighbourFacade,times(1)).pollSubscriptionStatus(any(),any());
	}

	@Test
	public void testPollingCreatedSubscriptionThatIsGone() {
		Subscription subscription = new Subscription(
				1,
				SubscriptionStatus.CREATED,
				"originatingCountry = 'NO'",
				"/mynode/subscriptions/1",
				"kyrre",
				Collections.emptySet()
		);
		Neighbour  neighbour = new Neighbour(
				"test",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								subscription
						)
				),
				new Connection()
		);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(1);
		when(backoffProperties.getNumberOfAttempts()).thenReturn(1);
		when(neighbourFacade.pollSubscriptionStatus(subscription,neighbour)).thenThrow(SubscriptionNotFoundException.class);
		neigbourDiscoveryService.pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour,neighbourFacade);
		assertThat(subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN));
	}

	@Test
	public void testPollSubscriptionGoneFromNeighbour() {

		Subscription subscription = new Subscription(
				1,
				SubscriptionStatus.CREATED,
				"originatingCountry = 'NO'",
				"/mynode/subscriptions/1",
				"kyrre",
				Collections.emptySet()
		);
		Neighbour  neighbour = new Neighbour(
				"test",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								subscription
						)
				),
				new Connection()
		);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(1);
		when(backoffProperties.getNumberOfAttempts()).thenReturn(1);
		when(neighbourFacade.pollSubscriptionStatus(subscription,neighbour)).thenThrow(SubscriptionNotFoundException.class);
		neigbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour,neighbourFacade);
		assertThat(subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN));
	}

}
