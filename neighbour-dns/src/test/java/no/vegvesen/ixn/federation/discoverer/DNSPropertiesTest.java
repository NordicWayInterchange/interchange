package no.vegvesen.ixn.federation.discoverer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DNSProperties.class})
@EnableConfigurationProperties
public class DNSPropertiesTest {

	@Autowired
	DNSProperties dnsProperties;

	@Test
	public void getDomainName() {
		assertThat(dnsProperties.getDomainName()).isEqualTo("itsinterchange.eu");
	}
}