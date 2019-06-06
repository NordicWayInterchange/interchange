package no.vegvesen.ixn.federation.discoverer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ActiveProfiles("real")
public class DnsTestApplication implements ApplicationRunner {

	@Autowired
	DNSFacadeInterface dnsFacade;

	public static void main(String[] args) {
		SpringApplication.run(DnsTestApplication.class);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("DNS neighbours " + dnsFacade.getNeighbours());
	}
}
