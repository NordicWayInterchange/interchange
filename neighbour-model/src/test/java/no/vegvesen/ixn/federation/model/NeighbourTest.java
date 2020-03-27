package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDateTime;

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

	@Test(expected = DiscoveryException.class)
	public void serverNameEndWithoutDot() {
		Neighbour ericsson = new Neighbour("ericsson.", null, null, null);
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
		Assertions.assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(localSubscriptionsUpdatedNow)).isTrue();
	}

	@Test
	public void neighbourJustHadSubscriptionRequestShouldNotCheckSubscriptionRequest() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		LocalDateTime now = LocalDateTime.now();
		fedIn.setSuccessfulRequest(now);
		Neighbour neighbour = new Neighbour("nice-neighbour", null, null, fedIn);
		Assertions.assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(now)).isFalse();
	}

	@Test
	public void failedSubscriptionRequest_firstSetsStart() {
		Neighbour neighbour = new Neighbour("the-best-neighbour-ever", null, null, null);
		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffStartTime()).isNotNull().isAfter(LocalDateTime.now().minusSeconds(3));
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(0);
		assertThat(neighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(1);
		assertThat(neighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getBackoffAttempts()).isEqualTo(2);
		assertThat(neighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.FAILED);

		neighbour.failedConnection(2);
		assertThat(neighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.UNREACHABLE);

		neighbour.failedConnection(2);
		assertThat(neighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.UNREACHABLE);
	}

}