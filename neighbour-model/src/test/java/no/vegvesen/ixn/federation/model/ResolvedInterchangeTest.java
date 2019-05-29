package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ResolvedInterchangeTest {

	@Test
	public void getControlChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
		DNSResolvedInterchange fullDomainName = new DNSResolvedInterchange("my-host", "my-domain.top", 1234, 5678);
		fullDomainName.setDomainName("my-domain.top");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top:1234/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top:5678/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
		DNSResolvedInterchange fullDomainName = new DNSResolvedInterchange("my-host", "my-domain.top", 443, 5671);
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
	}

	@Test
	public void getMessageAndControlChannelUrlWithDomainNameDefaultPorts() {
		DNSResolvedInterchange fullDomainName = new DNSResolvedInterchange("my-host", "my-domain.top", -1, -1);
		fullDomainName.setDomainName("my-domain.top");
		assertThat(fullDomainName.getControlChannelUrl("/alive")).isEqualTo("https://my-host.my-domain.top/alive");
		assertThat(fullDomainName.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
	}

	@Test
	public void expectedUrlIsCreated() {
		DNSResolvedInterchange ericsson = new DNSResolvedInterchange("ericsson", "itsinterchange.eu", 8080, 5671);
		String actualURL = ericsson.getControlChannelUrl("/");
		assertThat(actualURL).isEqualTo("https://ericsson.itsinterchange.eu:8080/");
	}

	@Test(expected = DiscoveryException.class)
	public void domainNameValidationStartWithoutDot() {
		Interchange ericsson = new Interchange("ericsson", null, null, null);
		ericsson.setDomainName(".itsinterchange.eu");
	}
}