package no.vegvesen.ixn.federation.discoverer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
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