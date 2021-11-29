package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class NeighbourTest {

	@Test
	public void getControlChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top:1234/alive");
	}

	@Test
	public void getControlChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("443");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
	}

	@Test
	public void getControlChannelUrlWithDomainNameDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
	}

	@Test
	public void expectedUrlIsCreated() {
		String expectedURL = "https://ericsson.itsinterchange.eu:8080/";
		Neighbour ericsson = new Neighbour("ericsson.itsinterchange.eu", null, null, null);
		ericsson.setControlChannelPort("8080");
		String actualURL = ericsson.getControlChannelUrl("/");
		assertThat(expectedURL).isEqualTo(actualURL);
	}

	@Test
	public void serverNameEndWithoutDot() {
		assertThatExceptionOfType(DiscoveryException.class).isThrownBy(() ->
				new Neighbour("ericsson.", null, null, null));
	}

	@Test
	public void getControlChannelUrlWithoutDomainNameAndSpecificPort() {
		Neighbour fullDomainName = new Neighbour("my-host", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		assertThat(fullDomainName.getControlChannelUrl("/thePath")).isEqualTo("https://my-host:1234/thePath");
	}

	@Test
	public void shouldCheckSubscriptionRequestsForUpdates_neighbourNeverHadSubscriptionRequestShouldCheckSubscriptionRequest() {
		Capabilities capabilities = new Capabilities();
		capabilities.setLastCapabilityExchange(LocalDateTime.now().minusHours(1));
		Neighbour neighbour = new Neighbour("nice-neighbour", capabilities, null, null);
		LocalDateTime localSubscriptionsUpdatedNow = LocalDateTime.now();
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(Optional.of(localSubscriptionsUpdatedNow))).isTrue();
	}

	@Test
	public void shouldCheckSubscriptionRequestsForUpdates_neighbourJustHadSubscriptionRequestShouldNotCheckSubscriptionRequest() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		LocalDateTime now = LocalDateTime.now();
		fedIn.setSuccessfulRequest(now);
		Capabilities capabilities = new Capabilities();
		capabilities.setLastCapabilityExchange(now.minusHours(1));
		Neighbour neighbour = new Neighbour("nice-neighbour", capabilities, null, fedIn, new Connection());
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(Optional.of(now))).isFalse();
	}

	@Test
	public void shouldCheckSubscriptionRequestsForUpdates_neighbourJustHadCapabilityExchangeShouldNotCheckSubscriptionRequest() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		LocalDateTime capabilityExchangeTimeNow = LocalDateTime.now();
		LocalDateTime localSubscriptionUpdateTimeBefore = capabilityExchangeTimeNow.minusSeconds(1);
		fedIn.setSuccessfulRequest(capabilityExchangeTimeNow);
		Capabilities capabilities = new Capabilities();
		capabilities.setLastCapabilityExchange(capabilityExchangeTimeNow);
		Neighbour neighbour = new Neighbour("nice-neighbour", capabilities, null, fedIn);
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(Optional.of(localSubscriptionUpdateTimeBefore))).isFalse();
	}

	@Test
	void shouldCheckSubscriptionRequestsForUpdates_noLocalSubscriptions_shouldNotCheck() {
		Neighbour neighbour = new Neighbour("nice-neighbour", null, null, null);
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(Optional.empty())).isFalse();
	}

	@Test
	public void shouldCheckSubscriptionRequestsForUpdates_neighbourWithNewCapabilityPostShouldCheckSubscriptionRequest() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		LocalDateTime capabilityExchangeTimeNow = LocalDateTime.now();
		LocalDateTime localSubscriptionUpdateTimeBeforeCapabilityPost = capabilityExchangeTimeNow.minusSeconds(1);
		LocalDateTime successfulSubscriptionRequestTimeBeforeCapabilityPost = capabilityExchangeTimeNow.minusSeconds(2);
		fedIn.setSuccessfulRequest(successfulSubscriptionRequestTimeBeforeCapabilityPost);
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>());
		capabilities.setLastCapabilityExchange(capabilityExchangeTimeNow);
		Neighbour neighbourWithNewerCapabilitiesThanSubscriptionRequest = new Neighbour("nice-neighbour", capabilities, null, fedIn);
		assertThat(neighbourWithNewerCapabilitiesThanSubscriptionRequest.shouldCheckSubscriptionRequestsForUpdates(Optional.of(localSubscriptionUpdateTimeBeforeCapabilityPost))).isTrue();
	}

	@Test
	public void needsOurUpdatedCapabilitiesIfLocalCapabilitiesAreNeverComputedAndNeighbourNeverSeen() {
		Neighbour neverSeen = new Neighbour();
		neverSeen.setName("never-seen");
		assertThat(neverSeen.needsOurUpdatedCapabilities(Optional.empty())).isTrue();
	}

	@Test
	public void doesNotNeedsOurCapabilitiesIfLocalCapabilitiesAreNeverComputedAndNeighbourSeen() {
		Neighbour seenYesterday = neighbourSeenYesterday();
		assertThat(seenYesterday.needsOurUpdatedCapabilities(Optional.empty())).isFalse();
	}

	@Test
	public void needsOurUpdatedCapabilitiesIfLocalCapabilitiesAreNewer() {
		Neighbour seenYesterday = neighbourSeenYesterday();
		assertThat(seenYesterday.needsOurUpdatedCapabilities(Optional.of(LocalDateTime.now().minusHours(1)))).isTrue();
	}

	@NonNull
	private Neighbour neighbourSeenYesterday() {
		Neighbour seenYesterday = new Neighbour();
		seenYesterday.setName("seen-yesterday");
		seenYesterday.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newHashSet()));
		seenYesterday.getCapabilities().setLastCapabilityExchange(LocalDateTime.now().minusDays(1));
		return seenYesterday;
	}

	@Test
	public void hasEstablishedSubscriptionsNull() {
		Neighbour neighbour = new Neighbour();
		assertThat(neighbour.hasEstablishedSubscriptions()).isFalse();
	}

	@Test
	public void hasEstablishedSubscriptionsEmpty() {
		Neighbour neighbour = new Neighbour();
		neighbour.setNeighbourRequestedSubscriptions(new SubscriptionRequest());
		assertThat(neighbour.hasEstablishedSubscriptions()).isFalse();
	}

	@Test
	public void hasEstablishedSubscriptionsStatusEnabled() {
		Neighbour neighbour = new Neighbour();
		neighbour.setNeighbourRequestedSubscriptions(
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,
						Collections.emptySet()));
		assertThat(neighbour.hasEstablishedSubscriptions()).isTrue();
	}

}