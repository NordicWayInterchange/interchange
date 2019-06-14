package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class InterchangeTest {

	@Test
	public void getControlChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
		Interchange fullDomainName = new Interchange("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		fullDomainName.setMessageChannelPort("5678");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top:1234/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top:5678/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
		Interchange fullDomainName = new Interchange("my-host.my-domain.top", null, null, null);
		fullDomainName.setControlChannelPort("443");
		fullDomainName.setMessageChannelPort("5671");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameDefaultPorts() {
		Interchange fullDomainName = new Interchange("my-host.my-domain.top", null, null, null);
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
	}

	@Test
	public void expectedUrlIsCreated() {
		String expectedURL = "https://ericsson.itsinterchange.eu:8080/";
		Interchange ericsson = new Interchange("ericsson.itsinterchange.eu", null, null, null);
		ericsson.setControlChannelPort("8080");
		String actualURL = ericsson.getControlChannelUrl("/");
		assertThat(expectedURL).isEqualTo(actualURL);
	}

	@Test(expected = DiscoveryException.class)
	public void serverNameEndWithoutDot() {
		Interchange ericsson = new Interchange("ericsson.", null, null, null);
	}

	@Test
	public void getMessageChannelUrlWithoutDomainNameAndSpecificPort() {
		Interchange fullDomainName = new Interchange("my-host", null, null, null);
		fullDomainName.setMessageChannelPort("5678");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host:5678/");
	}

	@Test
	public void getControlChannelUrlWithoutDomainNameAndSpecificPort() {
		Interchange fullDomainName = new Interchange("my-host", null, null, null);
		fullDomainName.setControlChannelPort("1234");
		assertThat(fullDomainName.getControlChannelUrl("/thePath")).isEqualTo("https://my-host:1234/thePath");
	}
}