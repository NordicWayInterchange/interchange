package no.vegvesen.ixn.geo;

import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {GeoLookupIT.Initializer.class})
public class GeoLookupIT extends DockerBaseIT {

	@ClassRule
	public static GenericContainer postgisContainer = getPostgisContainer("interchangenode/src/test/docker/postgis");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url: jdbc:postgresql://localhost:" + postgisContainer.getMappedPort(JDBC_PORT) + "/geolookup",
					"spring.datasource.username: geolookup",
					"spring.datasource.password: geolookup",
					"spring.datasource.driver-class-name: org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	DataSource dataSource;

	private GeoLookup geoLookup;

	@Before
	public void setup() {
		geoLookup = new GeoLookup(new JdbcTemplate(dataSource));
	}

	@Test
	public void getPointAtBorderNorwaySwedenReturnsTwoCountries() {
		List<String> countries = geoLookup.getCountries(60d, 12.5d);
		assertThat(countries).hasSize(2).contains("SE").contains("NO");
	}

	@Test
	public void getPointInTheNorthSeaReturnsNoCountries() {
		List<String> countries = geoLookup.getCountries(60d, 4.0d);
		assertThat(countries).hasSize(0);
	}

}