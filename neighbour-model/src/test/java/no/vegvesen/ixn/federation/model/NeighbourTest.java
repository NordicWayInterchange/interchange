package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class NeighbourTest {

	@Test
	public void getControlChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		fullDomainName.setMessageChannelPort("5678");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top:1234/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top:5678/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("443");
		fullDomainName.setMessageChannelPort("5671");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameDefaultPorts() {
		Neighbour fullDomainName = new Neighbour("my-host.my-domain.top", null, null, null);
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
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
		assertThatExceptionOfType(DiscoveryException.class).isThrownBy(() -> {
			new Neighbour("ericsson.", null, null, null);
		});
	}

	@Test
	public void getMessageChannelUrlWithoutDomainNameAndSpecificPort() {
		Neighbour fullDomainName = new Neighbour("my-host", null, null, null);
		fullDomainName.setMessageChannelPort("5678");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host:5678/");
	}

	@Test
	public void getControlChannelUrlWithoutDomainNameAndSpecificPort() {
		Neighbour fullDomainName = new Neighbour("my-host", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		assertThat(fullDomainName.getControlChannelUrl("/thePath")).isEqualTo("https://my-host:1234/thePath");
	}

	@Test
	public void neighbourNeverHadSubscriptionRequestShouldCheckSubscriptionRequest() {
		Neighbour neighbour = new Neighbour("nice-neighbour", null, null, null);
		LocalDateTime localSubscriptionsUpdatedNow = LocalDateTime.now();
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(localSubscriptionsUpdatedNow)).isTrue();
	}

	@Test
	public void neighbourJustHadSubscriptionRequestShouldNotCheckSubscriptionRequest() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		LocalDateTime now = LocalDateTime.now();
		fedIn.setSuccessfulRequest(now);
		Neighbour neighbour = new Neighbour("nice-neighbour", null, null, fedIn);
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(now)).isFalse();
	}

	@Test
	public void failedSubscriptionRequest_firstSetsStart() {
		Neighbour neighbour = new Neighbour("the-best-neighbour-ever", null, null, null);
		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffStartTime()).isNotNull().isAfter(LocalDateTime.now().minusSeconds(3));
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(0);
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(1);
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(2);
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

		neighbour.failedConnection(2);
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

		neighbour.okConnection();
		assertThat(neighbour.getConnectionStatus()).isEqualTo(ConnectionStatus.CONNECTED);
		assertThat(neighbour.getBackoffStartTime()).isNull();
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(0);
	}

	@Test
	public void calculatedNextPostAttemptTimeIsInCorrectInterval(){
		Neighbour ericsson = new Neighbour();
		LocalDateTime now = LocalDateTime.now();

		// Mocking the first backoff attempt, where the exponential is 0.
		double exponential = 0;
		long expectedBackoff = (long) Math.pow(2, exponential)*2; //

		System.out.println("LocalDataTime now: "+ now.toString());
		LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
		LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

		System.out.println("Lower limit: " + lowerLimit.toString());
		System.out.println("Upper limit: " + upperLimit.toString());

		ericsson.setBackoffAttempts(0);
		ericsson.setBackoffStart(now);

		LocalDateTime result = ericsson.getNextPostAttemptTime(60000, 2000);

		assertThat(result).isBetween(lowerLimit, upperLimit);
	}

	@Test
	public void needsOurUpdatedCapabilitiesIfLocalCapabilitiesAreNeverComputedAndNeighbourNeverSeen() {
		Neighbour neverSeen = new Neighbour();
		neverSeen.setName("never-seen");
		assertThat(neverSeen.needsOurUpdatedCapabilities(null)).isTrue();
	}

	@Test
	public void doesNotNeedsOurCapabilitiesIfLocalCapabilitiesAreNeverComputedAndNeighbourSeen() {
		Neighbour seenYesterday = neighbourSeenYesterday();
		assertThat(seenYesterday.needsOurUpdatedCapabilities(null)).isFalse();
	}

	@Test
	public void needsOurUpdatedCapabilitiesIfLocalCapabilitiesAreNewer() {
		Neighbour seenYesterday = neighbourSeenYesterday();
		assertThat(seenYesterday.needsOurUpdatedCapabilities(LocalDateTime.now().minusHours(1))).isTrue();
	}

	@NonNull
	private Neighbour neighbourSeenYesterday() {
		Neighbour seenYesterday = new Neighbour();
		seenYesterday.setName("seen-yesterday");
		seenYesterday.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newHashSet()));
		seenYesterday.getCapabilities().setLastCapabilityExchange(LocalDateTime.now().minusDays(1));
		return seenYesterday;
	}
}